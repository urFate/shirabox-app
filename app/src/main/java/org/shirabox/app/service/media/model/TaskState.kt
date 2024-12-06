package org.shirabox.app.service.media.model

enum class TaskState() {
    ENQUEUED,
    IN_PROGRESS,
    PAUSED,
    STOPPED,
    FINISHED,
    CONVERTING
}