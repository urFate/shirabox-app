package com.shirabox.shirabox.model

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistVideo(
    val streamUrls: Map<Quality, String>,
    val openingMarkers: Pair<Long, Long>
)