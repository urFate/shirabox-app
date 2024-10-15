package org.shirabox.app.service.media

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import org.shirabox.app.service.media.model.DownloadState
import org.shirabox.app.service.media.model.DownloadsListener
import org.shirabox.app.service.media.model.EnqueuedTask
import org.shirabox.app.service.media.model.MediaDownloadTask
import org.shirabox.core.db.AppDatabase
import org.shirabox.core.entity.DownloadEntity
import org.shirabox.core.media.HlsParser
import org.shirabox.core.media.MpegTools
import org.shirabox.core.model.StreamProtocol
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL


class DownloadsServiceHelper(
    private val coroutineScope: CoroutineScope,
    private val db: AppDatabase
) {
    private val _queriesList: MutableStateFlow<List<EnqueuedTask>> = MutableStateFlow(emptyList())
    private val listeners: MutableList<DownloadsListener> = mutableListOf()
    private val okHttpClient = OkHttpClient()

    fun enqueue(vararg tasks: MediaDownloadTask) {
        coroutineScope.launch {
            println("Initialing new query...")
            val query = tasks.map {
                EnqueuedTask(
                    mediaDownloadTask = it,
                )
            }

            _queriesList.update {
                it.toMutableList().apply { addAll(query) }
            }

            query.forEach {
                it.state.emit(DownloadState.ENQUEUED)
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

                if (enqueuedTask.state.value == DownloadState.STOPPED) continue

                Log.d("MDSH", "Started task")
                enqueuedTask.state.value = DownloadState.IN_PROGRESS

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
                    if (enqueuedTask.state.value != DownloadState.STOPPED) {
                        enqueuedTask.state.emit(DownloadState.FINISHED)
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

            val url = URL(mediaDownloadTask.url)
            val file = File(mediaDownloadTask.file)

            file.mkdirs()
            if (file.exists()) file.delete()
            file.createNewFile()

            val length = url.openConnection().apply {
                connect()
            }.contentLength

            val input = BufferedInputStream(url.openStream(), 8192)
            val output = FileOutputStream(mediaDownloadTask.file)
            val bytes = ByteArray(1024)
            var count = input.read(bytes)
            var total = 0L

            while (count != -1) {
                val pausedProgress = enqueuedTask.mediaDownloadTask.pausedProgress
                val currentProgress = total.toFloat() / length

                when (enqueuedTask.state.value) {
                    DownloadState.PAUSED -> {
                        input.close()
                        output.close()
                        throw ForcedInterruptionException()
                    }
                    DownloadState.STOPPED -> {
                        input.close()
                        output.close()
                        if (file.exists()) file.delete()

                        throw ForcedInterruptionException()
                    }
                    else -> {
                        if (pausedProgress != null && (pausedProgress <= currentProgress) ) {
                            total += count
                            count = input.read(bytes)
                            continue
                        }
                    }
                }

                total += count
                output.write(bytes, 0, count)
                count = input.read(bytes)

                val progress = total.toFloat() / length
                enqueuedTask.progressState.emit(progress)
            }

            output.flush()
            output.close()
            input.close()
        }
    }

    private suspend fun downloadHLS(enqueuedTask: EnqueuedTask) {
        withContext(Dispatchers.IO) {
            val mediaDownloadTask = enqueuedTask.mediaDownloadTask
            val pausedProgress = enqueuedTask.mediaDownloadTask.pausedProgress

            val segmentsList = HlsParser.parseUrl(mediaDownloadTask.url)

            val file = File(mediaDownloadTask.file)
            file.mkdirs()
            if (file.exists()) file.delete()
            file.createNewFile()

            val sink = file.sink().buffer()

            segmentsList.forEachIndexed { index, segmentUrl ->
                val progress = index.inc() / segmentsList.size.toFloat()

                // Skip cycle until we reach previous progress
                pausedProgress?.let {
                    if (progress <= pausedProgress) return@forEachIndexed
                }

                val request = Request.Builder().url(segmentUrl).build()
                val response = okHttpClient.newCall(request).execute()

                if (response.isSuccessful) {
                    response.body.bytes().forEach { byte ->
                        when (enqueuedTask.state.value) {
                            DownloadState.PAUSED -> {
                                response.close()
                                sink.close()

                                throw ForcedInterruptionException()
                            }
                            DownloadState.STOPPED -> {
                                response.close()
                                sink.close()
                                if (file.exists()) file.delete()

                                throw ForcedInterruptionException()
                            }
                            else -> sink.writeByte(byte.toInt())
                        }
                    }

                    sink.flush()
                    response.close()
                }

                enqueuedTask.progressState.emit(progress)
            }

            sink.close()

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
                .filter { it.state.value != DownloadState.STOPPED }
        }


    fun getEnqueuedTask(contentUid: Long, uid: Int?): Flow<EnqueuedTask?> =
        _queriesList.map { query ->
            query
                .filter { it.mediaDownloadTask.contentUid == contentUid }
                .lastOrNull { it.mediaDownloadTask.uid == uid }
        }

    fun pauseEnqueuedTask(task: EnqueuedTask) {
        coroutineScope.launch {
            task.state.value = DownloadState.PAUSED

            task.state.collect { state ->
                if (state == DownloadState.FINISHED) {
                    val mediaDownloadTask = task.mediaDownloadTask

                    db.downloadDao().insertDownload(
                        DownloadEntity(
                            url = mediaDownloadTask.url,
                            file = mediaDownloadTask.file,
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

    fun pauseGroup(contentUid: Long, groupId: String) {
        coroutineScope.launch {
            getQueryByGroupId(contentUid, groupId).last()?.forEach {
                pauseEnqueuedTask(it)
            }
        }
    }

    fun stopByGroupId(contentUid: Long, groupId: String) {
        _queriesList
            .value
            .filter { it.mediaDownloadTask.contentUid == contentUid }
            .filter { it.mediaDownloadTask.groupId == groupId }
            .forEach { it.state.value = DownloadState.STOPPED }
    }

    class ForcedInterruptionException : Exception()

    class MpegRepairmentFailureException : Exception()
}