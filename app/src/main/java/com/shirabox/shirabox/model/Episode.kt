package com.shirabox.shirabox.model

data class Episode (
    val name: String?,
    val extra: String = "",
    val episode: Int,
    val uploadTimestamp: Long,
    val videos: Map<Quality, String>? = null,
    val chapters: List<String>? = null,
    val videoMarkers: Pair<Long?, Long?>? = null,
    val type: ContentType
)