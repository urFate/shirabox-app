package org.shirabox.app.service.media.model

interface DownloadsListener {
    fun onCurrentTaskChanged(task: EnqueuedTask)
    fun onTaskFinish(task: EnqueuedTask, exception: Exception?)
    fun onLifecycleEnd()
}