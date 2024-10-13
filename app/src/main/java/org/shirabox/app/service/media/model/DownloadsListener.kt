package org.shirabox.app.service.media.model

import org.shirabox.app.service.media.DownloadsServiceHelper


interface DownloadsListener {
    fun onCurrentTaskChanged(task: EnqueuedTask, position: Int, querySize: Int)
    fun onTaskFinish(task: EnqueuedTask, exception: Exception?)
    fun onQueryFinish(downloadQuery: DownloadsServiceHelper.DownloadQuery, exception: Exception?)
    fun onLifecycleEnd()
}