package org.shirabox.app.service.media.model

import kotlinx.coroutines.flow.MutableStateFlow

data class EnqueuedTask(
    val mediaDownloadTask: MediaDownloadTask,
    val progressState: MutableStateFlow<Float> = MutableStateFlow(0.0f),
    val stopState: MutableStateFlow<Boolean> = MutableStateFlow(false),
)
