package org.shirabox.app.service.media.model

enum class DownloadState {
    ENQUEUED,
    IN_PROGRESS,
    FINISHED,
    STOPPED,
    PAUSED
}