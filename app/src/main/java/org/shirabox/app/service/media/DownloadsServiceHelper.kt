package org.shirabox.app.service.media

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import org.shirabox.app.service.media.model.DownloadsListener
import org.shirabox.app.service.media.model.EnqueuedTask
import org.shirabox.app.service.media.model.MediaDownloadTask
import org.shirabox.core.media.HlsParser
import org.shirabox.core.media.MpegTools
import org.shirabox.core.model.StreamProtocol
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.concurrent.CopyOnWriteArrayList


class DownloadsServiceHelper(
    private val coroutineScope: CoroutineScope
) {
    private val _queriesList: MutableStateFlow<List<DownloadQuery>> = MutableStateFlow(emptyList())
    private val listeners: MutableList<DownloadsListener> = mutableListOf()
    private val okHttpClient = OkHttpClient()

    fun enqueue(
        contentUid: Long,
        vararg tasks: MediaDownloadTask
    ) {
        coroutineScope.launch {
            println("Initialing new query...")
            val query = DownloadQuery(
                contentUid = contentUid,
                query = CopyOnWriteArrayList(
                    tasks.map {
                        EnqueuedTask(
                            mediaDownloadTask = it,
                        )
                    }
                )
            )

            _queriesList.update {
                it.toMutableList().apply { add(query) }
            }
            println("Queries: ${_queriesList.value.size}")
        }
    }

    /**
     * Starts queries downloading.
     * Should be called only after full service initialization.
     */
    suspend fun initQueryJob() {
        coroutineScope.launch {
            var queryListIterator = _queriesList.value.iterator()

            while (queryListIterator.hasNext()) {
                val downloadQuery = queryListIterator.next()
                var exception: Exception? = null

                val queryIterator = downloadQuery.query.listIterator()

                while (queryIterator.hasNext()) {
                    val index = queryIterator.nextIndex()
                    val enqueuedTask = queryIterator.next()

                    if (enqueuedTask.stopState.value) continue

                    Log.d("MDSH", "Started task")

                    launch {
                        // Notify listeners about current task
                        Log.d("MDSH", "Trying to notify listeners")

                        listeners.forEach {
                            it.onCurrentTaskChanged(
                                task = enqueuedTask,
                                position = index.inc(),
                                querySize = downloadQuery.query.size
                            )
                        }
                    }

                    try {
                        when (enqueuedTask.mediaDownloadTask.streamProtocol) {
                            StreamProtocol.MPEG -> download(enqueuedTask)
                            StreamProtocol.HLS -> downloadHLS(enqueuedTask)
                        }

                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        exception = ex
                    } finally {
                        listeners.forEach { it.onTaskFinish(enqueuedTask, exception) }
                        enqueuedTask.stopState.emit(true)
                    }
                }

                listeners.forEach {
                    it.onQueryFinish(
                        downloadQuery = downloadQuery,
                        exception = exception
                    )

                    downloadQuery.query.clear()
                }

                _queriesList.update {
                    it.toMutableList().apply { remove(downloadQuery) }
                }

                queryListIterator = _queriesList.value.listIterator()
            }

            if (_queriesList.value.isEmpty()) {
                listeners.forEach { it.onLifecycleEnd() }
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
                if (enqueuedTask.stopState.value) {
                    Log.d("MDSH", "Forcing stop...")
                    input.close()
                    if (file.exists()) file.delete()

                    throw ForcedInterruptionException()
                }

                total += count
                output.write(bytes, 0, count)

                val progress = total.toFloat() / length
                count = input.read(bytes)

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

            val segmentsList = HlsParser.parseUrl(mediaDownloadTask.url)

            val file = File(mediaDownloadTask.file)
            file.mkdirs()
            if (file.exists()) file.delete()
            file.createNewFile()

            val sink = file.sink().buffer()

            segmentsList.forEachIndexed { index, segmentUrl ->
                val request = Request.Builder().url(segmentUrl).build()
                val response = okHttpClient.newCall(request).execute()

                if (response.isSuccessful) {
                    response.body.bytes().forEach { byte ->
                        if(enqueuedTask.stopState.value) {
                            response.close()
                            sink.close()
                            if (file.exists()) file.delete()

                            throw ForcedInterruptionException()
                        }

                        sink.writeByte(byte.toInt())
                    }

                    sink.flush()
                    response.close()
                }

                enqueuedTask.progressState.emit(index.inc() / segmentsList.size.toFloat())
            }

            sink.close()

            MpegTools.repairMpeg(mediaDownloadTask.file) { isSuccessful ->
                if (!isSuccessful) throw MpegRepairmentFailureException()
            }
        }
    }

    fun addListener(listener: DownloadsListener) = listeners.add(listener)

    fun removeListener(listener: DownloadsListener) = listeners.remove(listener)

    fun getQuery(): Flow<List<DownloadQuery>> = _queriesList

    fun getQueryByGroupId(contentUid: Long, groupId: String): Flow<CopyOnWriteArrayList<EnqueuedTask>?> =
        _queriesList.map { queries ->
            queries
                .filter { it.contentUid == contentUid }
                .firstOrNull { it.query.any { task -> task.mediaDownloadTask.group == groupId } }
                ?.query
        }


    fun getEnqueuedTask(contentUid: Long, uid: Int?): Flow<EnqueuedTask?> =
        _queriesList.map { queryList ->
            queryList
                .firstOrNull { it.contentUid == contentUid }
                ?.query
                ?.firstOrNull { it.mediaDownloadTask.uid == uid && !it.stopState.value }
        }


    suspend fun stopByGroupId(contentUid: Long, groupId: String) {
        _queriesList
            .value
            .filter { it.contentUid == contentUid }
            .filter { it.query.any { task -> task.mediaDownloadTask.group == groupId } }
            .forEach { it.query.forEach { task -> task.stopState.emit(true) } }
    }

    data class DownloadQuery(
        val contentUid: Long,
        val query: CopyOnWriteArrayList<EnqueuedTask>
    )

    class ForcedInterruptionException : Exception()

    class MpegRepairmentFailureException : Exception()
}