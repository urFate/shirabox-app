package org.shirabox.core.model

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistVideo(
    val episode: Int,
    val streamUrls: Map<Quality, String>,
    val openingMarkers: Pair<Long, Long>
)