package org.shirabox.app.service.media.model

import org.shirabox.core.model.Content
import org.shirabox.core.model.Quality
import org.shirabox.core.model.StreamProtocol

data class MediaDownloadTask(
    val uid: Int,
    val url: String,
    val file: String,
    val quality: Quality,
    val pausedProgress: Float? = null,
    val streamProtocol: StreamProtocol,
    val groupId: String,
    val content: Content,
    val contentUid: Long,
)