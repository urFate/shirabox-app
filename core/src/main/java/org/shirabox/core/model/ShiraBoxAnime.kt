package org.shirabox.core.model

data class ShiraBoxAnime(
    val id: Int,
    val name: String,
    val image: String,
    val russianName: String,
    val schedule: Schedule,
    val shikimoriId: Int
) {
    data class Schedule(
        val releaseRange: List<Long>,
        val released: Boolean
    )
}
