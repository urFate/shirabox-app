package org.shirabox.core.model

data class ScheduleEntry(
    val id: Int,
    val image: String,
    val name: String,
    val russianName: String,
    val nextEpisodeNumber: Int,
    val released: Boolean,
    val releaseRange: List<Long>,
    val shikimoriId: Int
)