package org.shirabox.app.service.media

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.shirabox.app.App
import org.shirabox.app.service.media.model.DownloadsListener
import org.shirabox.app.service.media.model.EnqueuedTask
import org.shirabox.app.service.media.model.MediaDownloadTask
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
import java.net.URL


object DownloadsServiceHelper {
    private val _queriesList: MutableStateFlow<List<EnqueuedTask>> = MutableStateFlow(emptyList())
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val listeners: MutableList<DownloadsListener> = mutableListOf()

    fun enqueue(db: AppDatabase, vararg tasks: MediaDownloadTask) {
        coroutineScope.launch {
            println("Initialing new query...")

            val pausedTasks = db.downloadDao().allSingleWithContent()
                .filter { pausedTask -> tasks.any { it.uid == pausedTask.downloadEntity.episodeUid } }

            val query = tasks
                .filter { task -> pausedTasks.none { it.downloadEntity.episodeUid == task.uid } }
                .map {
                    EnqueuedTask(
                        mediaDownloadTask = it,
                    )
                }
                .toMutableList()
                .apply {
                    addAll(
                        pausedTasks.map {
                            val mediaDownloadTask = MediaDownloadTask(
                                uid = it.downloadEntity.episodeUid,
                                url = it.downloadEntity.url,
                                file = it.downloadEntity.file,
                                startFrom = it.downloadEntity.mpegBytes,
                                startFromFragment = it.downloadEntity.hlsFragment,
                                quality = it.downloadEntity.quality,
                                streamProtocol = it.downloadEntity.streamProtocol,
                                groupId = it.downloadEntity.group,
                                content = Util.mapEntityToContent(it.contentEntity),
                                contentUid = it.contentEntity.uid
                            )

                            EnqueuedTask(
                                mediaDownloadTask = mediaDownloadTask,
                                pausedBytes = mediaDownloadTask.startFrom,
                                pausedHlsFragment = mediaDownloadTask.startFromFragment
                            )
                        }
                    )
                }

            _queriesList.update {
                it.toMutableList().apply { addAll(query) }
            }

            query.forEach {
                it.state.emit(TaskState.ENQUEUED)
            }

            println("Queries:")
            _queriesList.value.forEach {
                println(it)
            }
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
                    TaskState.STOPPED, TaskState.FINISHED, TaskState.PAUSED -> continue
                    else -> enqueuedTask.state.value = TaskState.IN_PROGRESS
                }

                Log.d("MDSH", "Started task")

                launch {
                    // Notify listeners about current task
                    Log.d("MDSH", "Trying to notify listeners")

                    listeners.forEach {
                        it.onCurrentTaskChanged(enqueuedTask)
                    }
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

            println("Out of the cycle")

            listeners.forEach { it.onLifecycleEnd() }
            _queriesList.update {
                it.toMutableList().apply { clear() }
            }
        }
    }

    private suspend fun download(enqueuedTask: EnqueuedTask) {
        withContext(Dispatchers.IO) {
            val mediaDownloadTask = enqueuedTask.mediaDownloadTask
            val pausedBytes = enqueuedTask.pausedBytes ?: 0L

            val url = URL(mediaDownloadTask.url)
            val file = File(mediaDownloadTask.file)

            file.parentFile?.mkdirs()
            if (!file.exists()) file.createNewFile()

            val length = url.openConnection().apply {
                if (pausedBytes > 0L) setRequestProperty("Range", "bytes=$pausedBytes-")
                connect()
            }.contentLengthLong

            val outputStream = FileOutputStream(file, true)
            var total = pausedBytes

            try {
                val inputStream = url.openStream()

                BufferedInputStream(inputStream, 8192).use { input ->
                    val bytes = ByteArray(1024)
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

                                val progress = total.toFloat() / length
                                println("Progress updated: $progress - $total / $length")

                                enqueuedTask.progressState.emit(progress)
                            }
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

            outputStream.flush()
            outputStream.close()
        }
    }

    private suspend fun downloadHLS(enqueuedTask: EnqueuedTask) {
        withContext(Dispatchers.IO) {
            val mediaDownloadTask = enqueuedTask.mediaDownloadTask
            val pausedBytes = enqueuedTask.pausedBytes ?: 0L
            val pausedHlsFragment = enqueuedTask.pausedHlsFragment

            val file = File(mediaDownloadTask.file)
            file.parentFile?.mkdirs()
            if (!file.exists()) file.createNewFile()

            val segmentsList = HlsParser.parseUrl(mediaDownloadTask.url)
            val outputStream = FileOutputStream(file, true)

            segmentsList.forEachIndexed { index, segmentUrl ->
                // Skip downloaded fragments
                pausedHlsFragment?.let { fragment ->
                    if (fragment > index) {
                        println("Skipping $index fragment...")
                        return@forEachIndexed
                    }
                }

                val isPausedFragment = pausedBytes > 0L && pausedHlsFragment == index

                val url = URL(segmentUrl)
                val progress = index.inc() / segmentsList.size.toFloat()

                url.openConnection().apply {
                    if (isPausedFragment) setRequestProperty("Range", "bytes=$pausedBytes-")
                    connect()
                }

                var total = pausedBytes

                try {
                    val inputStream = url.openStream()

                    BufferedInputStream(inputStream, 8192).use { input ->
                        val bytes = ByteArray(1024)
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
                    }

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


            outputStream.close()

            enqueuedTask.progressState.emit(0F)
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
                if (state == TaskState.FINISHED && task.pausedBytes != null) {

                    db.downloadDao().insertDownload(
                        DownloadEntity(
                            url = mediaDownloadTask.url,
                            file = mediaDownloadTask.file,
                            mpegBytes = task.pausedBytes!!,
                            hlsFragment = task.pausedHlsFragment,
                            pausedProgress = task.progressState.value,
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
            _queriesList.value.forEach {
                pauseEnqueuedTask(db, it)
            }
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
        enqueuedTask.pausedBytes = total
        fragment?.let { enqueuedTask.pausedHlsFragment = fragment }
    }

    fun pauseGroup(db: AppDatabase, contentUid: Long, groupId: String) {
        coroutineScope.launch {
            getQueryByGroupId(contentUid, groupId).last()?.forEach {
                pauseEnqueuedTask(db, it)
            }
        }
    }

    fun stopByGroupId(contentUid: Long, groupId: String) {
        _queriesList
            .value
            .filter { it.mediaDownloadTask.contentUid == contentUid }
            .filter { it.mediaDownloadTask.groupId == groupId }
            .forEach { it.state.value = TaskState.STOPPED }
    }

    class ForcedInterruptionException : Exception()

    class MpegRepairmentFailureException : Exception()
}