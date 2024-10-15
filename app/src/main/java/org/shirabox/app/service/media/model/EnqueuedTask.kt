package org.shirabox.app.service.media.model

import kotlinx.coroutines.flow.MutableStateFlow

data class EnqueuedTask(
    val mediaDownloadTask: MediaDownloadTask,
    val state: MutableStateFlow<DownloadState> = MutableStateFlow(DownloadState.ENQUEUED),
    val progressState: MutableStateFlow<Float> = MutableStateFlow(0.0f),
)