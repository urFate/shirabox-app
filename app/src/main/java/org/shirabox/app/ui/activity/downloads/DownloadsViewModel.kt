package org.shirabox.app.ui.activity.downloads

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.shirabox.app.R
import org.shirabox.app.service.media.DownloadsServiceHelper
import org.shirabox.app.service.media.MediaDownloadsService
import org.shirabox.app.service.media.model.EnqueuedTask
import org.shirabox.app.service.media.model.MediaDownloadTask
import org.shirabox.app.service.media.model.PauseData
import org.shirabox.app.service.media.model.TaskState
import org.shirabox.core.db.AppDatabase
import org.shirabox.core.entity.ContentEntity
import org.shirabox.core.entity.DownloadEntity
import org.shirabox.core.entity.EpisodeEntity
import org.shirabox.core.model.Content
import org.shirabox.core.util.Util
import java.io.File
import javax.inject.Inject
import kotlin.collections.groupBy
import kotlin.collections.map

@HiltViewModel
class DownloadsViewModel @Inject constructor(@ApplicationContext context: Context) : ViewModel() {
    val db = AppDatabase.getAppDataBase(context)!!
    private val helper = DownloadsServiceHelper
    val offlineFlowFilter = MutableStateFlow("")

    fun sortedQueryFlow(): Flow<Map<Content, Map<String, List<EnqueuedTask>>>> =
        helper.getQuery().map { taskList ->
            taskList
                .groupBy { it.mediaDownloadTask.content }
                .mapValues { entry ->
                    entry.value
                        .groupBy { it.mediaDownloadTask.groupId }
                        .mapValues { it.value }
                }
        }

    fun pausedTasksFlow(): Flow<Map<ContentEntity, Map<String, List<DownloadEntity>>>> =
        db.downloadDao().allWithContent().map { list ->
            list.sortedByDescending { it.downloadEntity.pausedProgress }
                .groupBy { it.contentEntity }
                .mapValues {
                    it.value.map { it.downloadEntity }
                        .groupBy { it.group }
                }
        }

    fun offlineEpisodesFlow(): Flow<Map<ContentEntity, Map<String, List<EpisodeEntity>>>> =
        db.episodeDao().getOfflineEpisodesWithContent().map { list ->
            list
                .groupBy { it.contentEntity }
                .mapValues {
                    it.value
                        .map { it.episodeEntity }
                        .groupBy { it.actingTeamName }
                }
        }

    fun calculateEpisodesSize(files: List<String>): Flow<Long> = flow {
        var totalBytes = 0L

        files.forEach { path -> totalBytes += File(path).length() }
        emit(totalBytes)
    }

    fun pauseQuery() = DownloadsServiceHelper.pauseQuery(db)

    fun pauseTask(enqueuedTask: EnqueuedTask) = DownloadsServiceHelper.pauseEnqueuedTask(
        db = db,
        task = enqueuedTask
    )

    fun resumeTasks(context: Context, vararg entities: Pair<ContentEntity, DownloadEntity>) {
        if (!Util.isNetworkAvailable(context)) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    context,
                    R.string.no_internet_connection,
                    Toast.LENGTH_LONG
                ).show()
            }

            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            helper.enqueue(
                db = db,
                entities.map { contentAndDownload ->
                    MediaDownloadTask(
                        uid = contentAndDownload.second.episodeUid,
                        url = contentAndDownload.second.url,
                        file = contentAndDownload.second.file,
                        quality = contentAndDownload.second.quality,
                        pauseData = PauseData(
                            progress = contentAndDownload.second.pausedProgress,
                            bytes = contentAndDownload.second.mpegBytes,
                            fragment = contentAndDownload.second.hlsFragment,
                        ),
                        streamProtocol = contentAndDownload.second.streamProtocol,
                        groupId = contentAndDownload.second.group,
                        content = Util.mapEntityToContent(contentAndDownload.first),
                        contentUid = contentAndDownload.first.uid
                    )
                }
            )

            entities.forEach { contentAndDownload ->
                launch { db.downloadDao().deleteDownload(contentAndDownload.second) }
            }

            context.startService(Intent(context, MediaDownloadsService::class.java))
        }
    }

    fun resumeAllTasks(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            resumeTasks(
                context = context,
                entities = db.downloadDao().allSingleWithContent()
                    .map { it.contentEntity to it.downloadEntity }.toTypedArray()
            )
        }
    }

    fun cancelPausedTasks(vararg entities: Pair<ContentEntity, DownloadEntity>) {
        entities.forEach { contentAndDownload ->
            viewModelScope.launch(Dispatchers.IO) {
                val file = File(contentAndDownload.second.file)
                if (file.exists()) file.delete()

                db.downloadDao().deleteDownload(contentAndDownload.second)
            }
        }
    }

    fun cancelEnqueuedTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            helper.getQuery().first().forEach { it.state.emit(TaskState.STOPPED) }
        }
    }

    fun cancelAllPausedTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            db.downloadDao().allSingle().forEach {
                val file = File(it.file)
                if (file.exists()) file.delete()

                db.downloadDao().deleteDownload(it)
            }
        }
    }

    fun deleteOfflineEpisodes(vararg entities: EpisodeEntity) {
        entities.forEach { entity ->
            viewModelScope.launch(Dispatchers.IO) {
                entity.offlineVideos?.forEach {
                    val file = File(it.value)
                    if (file.exists()) file.delete()
                }

                db.episodeDao().updateEpisodes(entity.copy(offlineVideos = null))
            }
        }
    }

    fun episodesFlow(uid: Int) = db.episodeDao().getEpisodeFlowByUid(uid)
}