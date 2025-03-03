package org.shirabox.app.service.media

import com.google.common.net.InternetDomainName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.shirabox.app.App
import org.shirabox.app.service.media.model.DownloadsListener
import org.shirabox.app.service.media.model.EnqueuedTask
import org.shirabox.app.service.media.model.MediaDownloadTask
import org.shirabox.app.service.media.model.PauseData
import org.shirabox.app.service.media.model.TaskState
import org.shirabox.core.db.AppDatabase
import org.shirabox.core.entity.DownloadEntity
import org.shirabox.core.media.HlsParser
import org.shirabox.core.media.MpegTools
import org.shirabox.core.model.StreamProtocol
import org.shirabox.core.util.Util
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URI


object DownloadsServiceHelper {
    private val okHttpClient = OkHttpClient()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val listeners: MutableList<DownloadsListener> = mutableListOf()
    private const val BUFFER_SIZE = 8192

    private val _queriesList: MutableStateFlow<List<EnqueuedTask>> = MutableStateFlow(emptyList())

    fun enqueue(db: AppDatabase, tasks: List<MediaDownloadTask>) {
        coroutineScope.launch {
            val pausedTasks = db.downloadDao().allSingleWithContent()
                .filter { pausedTask -> tasks.any { it.uid == pausedTask.downloadEntity.episodeUid } }

            val query = tasks
                .filter { task -> pausedTasks.none { it.downloadEntity.episodeUid == task.uid } }
                .map(::EnqueuedTask)
                .toMutableList()
                .apply {
                    addAll(
                        pausedTasks.map {
                            val mediaDownloadTask = MediaDownloadTask(
                                uid = it.downloadEntity.episodeUid,
                                url = it.downloadEntity.url,
                                file = it.downloadEntity.file,
                                pauseData = PauseData(
                                    progress = it.downloadEntity.pausedProgress,
                                    bytes = it.downloadEntity.mpegBytes,
                                    fragment = it.downloadEntity.hlsFragment,
                                ),
                                quality = it.downloadEntity.quality,
                                streamProtocol = it.downloadEntity.streamProtocol,
                                groupId = it.downloadEntity.group,
                                content = Util.mapEntityToContent(it.contentEntity),
                                contentUid = it.contentEntity.uid
                            )

                            EnqueuedTask(
                                mediaDownloadTask = mediaDownloadTask,
                                progressState = MutableStateFlow(mediaDownloadTask.pauseData?.progress ?: 0F),
                            )
                        }
                    )
                }

            _queriesList.update {
                it.toMutableList()
                    .apply { addAll(query) }
                    .sortedByDescending { it.mediaDownloadTask.pauseData?.progress }
            }

            query.forEach { it.state.emit(TaskState.ENQUEUED) }
        }
    }

    /**
     * Starts queries downloading.
     * Should be called only after full service initialization.
     */
    internal fun initQueryJob() {
        coroutineScope.launch {
            var queryListIterator = _queriesList.value.listIterator()

            while (queryListIterator.hasNext()) {
                val enqueuedTask = queryListIterator.next()
                val exceptions: MutableMap<EnqueuedTask, Exception> = mutableMapOf()

                when (enqueuedTask.state.value) {
                    TaskState.STOPPED, TaskState.FINISHED -> continue
                    TaskState.PAUSED -> {
                        enqueuedTask.state.emit(TaskState.FINISHED)
                        continue
                    }
                    else -> enqueuedTask.state.emit(TaskState.IN_PROGRESS)
                }

                launch {
                    // Notify listeners about current task
                    listeners.forEach { it.onCurrentTaskChanged(enqueuedTask) }
                }

                try {
                    when (enqueuedTask.mediaDownloadTask.streamProtocol) {
                        StreamProtocol.MPEG -> download(enqueuedTask)
                        StreamProtocol.HLS -> downloadHLS(enqueuedTask)
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    exceptions[enqueuedTask] = ex
                } finally {
                    listeners.forEach {
                        it.onTaskFinish(
                            enqueuedTask,
                            exceptions.getOrDefault(enqueuedTask, null)
                        )
                    }
                    if (enqueuedTask.state.value != TaskState.STOPPED) {
                        enqueuedTask.state.emit(TaskState.FINISHED)
                    }
                }

                queryListIterator = _queriesList.value.listIterator()
            }


            listeners.forEach { it.onLifecycleEnd() }
            _queriesList.update {
                it.toMutableList().apply { clear() }
            }
        }
    }

    private suspend fun download(enqueuedTask: EnqueuedTask) {
        withContext(Dispatchers.IO) {
            val mediaDownloadTask = enqueuedTask.mediaDownloadTask
            val pauseData = mediaDownloadTask.pauseData
            val pausedBytes = pauseData?.bytes ?: 0L
            val append = pauseData != null
            var total = pausedBytes

            val file = File(mediaDownloadTask.file)

            file.parentFile?.mkdirs()
            if (file.exists() && mediaDownloadTask.pauseData == null) file.delete()
            if (!file.exists()) file.createNewFile()

            val host = URI(mediaDownloadTask.url).host
            val topDomain = InternetDomainName.from(host).topPrivateDomain()

            val request = Request.Builder()
                .url(mediaDownloadTask.url)
                .addHeader("Range", "bytes=$pausedBytes-")
                .apply {
                    mpegHeaders("https://$topDomain/").forEach { addHeader(it.key, it.value) }
                }
                .build()

            val responseBody = okHttpClient.newCall(request).execute().body
            val length = responseBody.contentLength()

            val inputStream = responseBody.byteStream()
            val outputStream = FileOutputStream(file, append)

            BufferedInputStream(inputStream, BUFFER_SIZE).use { input ->
                try {
                    val bytes = ByteArray(BUFFER_SIZE)
                    var count = input.read(bytes)

                    while (count != -1) {
                        when (enqueuedTask.state.value) {
                            TaskState.PAUSED -> {
                                preparePausing(
                                    output = outputStream,
                                    enqueuedTask = enqueuedTask,
                                    total = total,
                                    fragment = null
                                )

                                throw ForcedInterruptionException()
                            }

                            TaskState.STOPPED -> {
                                input.close()
                                outputStream.close()
                                if (file.exists()) file.delete()

                                throw ForcedInterruptionException()
                            }

                            else -> {
                                total += count
                                outputStream.write(bytes, 0, count)
                                count = input.read(bytes)

                                val progress = total.toFloat() / (length + pausedBytes)

                                enqueuedTask.progressState.emit(progress)
                            }
                        }
                    }
                } catch (exception: Exception) {
                    if (exception !is ForcedInterruptionException) {
                        preparePausing(
                            output = outputStream,
                            enqueuedTask = enqueuedTask,
                            total = total,
                            fragment = null
                        )

                        pauseEnqueuedTask(App.appDatabase, enqueuedTask)
                    }
                    throw exception
                }
            }

            outputStream.flush()
            outputStream.close()
        }
    }

    private suspend fun downloadHLS(enqueuedTask: EnqueuedTask) {
        withContext(Dispatchers.IO) {
            val mediaDownloadTask = enqueuedTask.mediaDownloadTask
            val pausedBytes = mediaDownloadTask.pauseData?.bytes ?: 0L
            val pausedHlsFragment = mediaDownloadTask.pauseData?.fragment

            val file = File(mediaDownloadTask.file)
            file.parentFile?.mkdirs()
            if (!file.exists()) file.createNewFile()

            val segmentsList = HlsParser.parseUrl(mediaDownloadTask.url)
            val outputStream = FileOutputStream(file, true)

            segmentsList.forEachIndexed { index, segmentUrl ->
                // Skip saved fragments
                pausedHlsFragment?.let { fragment ->
                    if (fragment > index) return@forEachIndexed
                }

                val isPausedFragment = pausedBytes > 0L && pausedHlsFragment == index

                val progress = index.inc() / segmentsList.size.toFloat()
                var total = if (isPausedFragment) pausedBytes else 0

                val request = Request.Builder().url(segmentUrl).addHeader("Range", "bytes=$pausedBytes-").build()
                val responseBody = okHttpClient.newCall(request).execute().body

                try {
                    val inputStream = responseBody.byteStream()

                    BufferedInputStream(inputStream, BUFFER_SIZE).use { input ->
                        try {
                            val bytes = ByteArray(BUFFER_SIZE)
                            var count = input.read(bytes)

                            while (count != -1) {
                                when (enqueuedTask.state.value) {
                                    TaskState.PAUSED -> {
                                        preparePausing(
                                            output = outputStream,
                                            enqueuedTask = enqueuedTask,
                                            total = total,
                                            fragment = index
                                        )
                                        throw ForcedInterruptionException()
                                    }

                                    TaskState.STOPPED -> {
                                        input.close()
                                        outputStream.flush()
                                        outputStream.close()
                                        if (file.exists()) file.delete()

                                        throw ForcedInterruptionException()
                                    }

                                    else -> {
                                        total += count
                                        outputStream.write(bytes, 0, count)
                                        count = input.read(bytes)

                                        enqueuedTask.progressState.emit(progress)
                                    }
                                }
                            }

                            outputStream.flush()
                        } catch (exception: Exception) {
                            if (exception !is ForcedInterruptionException) {
                                preparePausing(
                                    output = outputStream,
                                    enqueuedTask = enqueuedTask,
                                    total = total,
                                    fragment = index
                                )

                                pauseEnqueuedTask(App.appDatabase, enqueuedTask)
                            }
                            throw exception
                        }
                    }
                } catch (exception: Exception) {
                    preparePausing(
                        output = outputStream,
                        enqueuedTask = enqueuedTask,
                        total = total,
                        fragment = index
                    )
                    throw exception
                }
            }

            outputStream.close()
            enqueuedTask.state.emit(TaskState.CONVERTING)

            MpegTools.repairMpeg(mediaDownloadTask.file) { isSuccessful ->
                if (!isSuccessful) throw MpegRepairmentFailureException()
            }
        }
    }

    fun addListener(listener: DownloadsListener) = listeners.add(listener)

    fun removeListener(listener: DownloadsListener) = listeners.remove(listener)

    fun getQuery(): Flow<List<EnqueuedTask>> = _queriesList

    fun getQueryByGroupId(contentUid: Long, groupId: String): Flow<List<EnqueuedTask>?> =
        _queriesList.map { query ->
            query
                .filter { it.mediaDownloadTask.contentUid == contentUid }
                .filter { it.mediaDownloadTask.groupId == groupId }
                .filter { it.state.value != TaskState.STOPPED }
        }


    fun getEnqueuedTask(contentUid: Long, uid: Int?): Flow<EnqueuedTask?> =
        _queriesList.map { query ->
            query
                .filter { it.mediaDownloadTask.contentUid == contentUid }
                .lastOrNull { it.mediaDownloadTask.uid == uid }
        }

    fun pauseEnqueuedTask(db: AppDatabase, task: EnqueuedTask) {
        coroutineScope.launch {
            task.state.value = TaskState.PAUSED
            val mediaDownloadTask = task.mediaDownloadTask

            task.state.collect { state ->
                if (state == TaskState.FINISHED) {
                    // Use initial pause data if task isn't started before be paused
                    val pausedProgress = task.mediaDownloadTask.pauseData?.progress ?: 0F
                    val pausedBytes = task.mediaDownloadTask.pauseData?.bytes ?: 0L
                    val pausedFragment = task.mediaDownloadTask.pauseData?.fragment

                    db.downloadDao().insertDownload(
                        DownloadEntity(
                            url = mediaDownloadTask.url,
                            file = mediaDownloadTask.file,
                            mpegBytes = pausedBytes,
                            hlsFragment = pausedFragment,
                            pausedProgress = pausedProgress,
                            quality = mediaDownloadTask.quality,
                            streamProtocol = mediaDownloadTask.streamProtocol,
                            group = mediaDownloadTask.groupId,
                            contentUid = mediaDownloadTask.contentUid,
                            episodeUid = mediaDownloadTask.uid
                        )
                    )
                }
            }
        }
    }

    fun pauseQuery(db: AppDatabase) {
        coroutineScope.launch {
            _queriesList.value
                .filter { it.state.value != TaskState.FINISHED }
                .forEach { pauseEnqueuedTask(db, it) }
        }
    }

    private fun preparePausing(
        output: FileOutputStream,
        enqueuedTask: EnqueuedTask,
        total: Long,
        fragment: Int?
    ) {
        output.flush()
        output.close()
        enqueuedTask.mediaDownloadTask.pauseData = PauseData(
            progress = enqueuedTask.progressState.value,
            bytes = total,
            fragment = fragment
        )
    }

    fun stopByGroupId(contentUid: Long, groupId: String) {
        _queriesList
            .value
            .filter { it.mediaDownloadTask.contentUid == contentUid }
            .filter { it.mediaDownloadTask.groupId == groupId }
            .forEach { it.state.value = TaskState.STOPPED }
    }

    fun mpegHeaders(rootHost: String): Map<String, String> = mapOf(
        "Accept" to "*/*",
        "Accept-Encoding" to "identity;q=1, *;q=0",
        "Origin" to rootHost,
        "Priority" to "i",
        "Referer" to rootHost,
        "Sec-Ch-Ua" to "\"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\"",
        "Sec-Ch-Ua-Mobile" to "?0",
        "Sec-Ch-Ua-Platform" to "\"Windows\"",
        "Sec-Fetch-Dest" to "video",
        "Sec-Fetch-Mode" to "cors",
        "Sec-Fetch-Site" to "same-site",
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
    )

    class ForcedInterruptionException : Exception()

    class MpegRepairmentFailureException : Exception()
}