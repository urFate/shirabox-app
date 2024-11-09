package org.shirabox.app.ui.activity.downloads

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.shirabox.app.service.media.DownloadsServiceHelper
import org.shirabox.app.service.media.model.EnqueuedTask
import org.shirabox.core.db.AppDatabase
import org.shirabox.core.entity.ContentEntity
import org.shirabox.core.entity.DownloadEntity
import org.shirabox.core.model.Content
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(@ApplicationContext context: Context) : ViewModel() {
    val db = AppDatabase.getAppDataBase(context)!!
    val helper = DownloadsServiceHelper

    fun queryFlow(): Flow<Map<Content, Map<String, List<EnqueuedTask>>>> =
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
            list.groupBy { it.contentEntity }
                .mapValues {
                    it.value.map { it.downloadEntity }
                        .groupBy { it.group }
                }
        }

    fun pauseQuery() = DownloadsServiceHelper.pauseQuery(db)

    fun episodesFlow(uid: Int) = db.episodeDao().getEpisodeFlowByUid(uid)

}