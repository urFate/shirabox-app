package com.shirabox.shirabox.model

data class Episode (
    val name: String?,
    val extra: String = "",
    val episode: Int,
    val uploadTimestamp: Long,
    val contents: List<String>,
    val videoMarkers: Pair<Long?, Long?>? = null,
    val type: ContentType
)