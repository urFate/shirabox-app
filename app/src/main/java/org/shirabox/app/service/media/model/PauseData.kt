package org.shirabox.app.service.media.model

data class PauseData(
    val progress: Float = 0F,
    var bytes: Long,
    var fragment: Int?
)
