package org.shirabox.data.shirabox

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ScheduleList(
    @SerialName("schedule")
    val schedule: List<ShiraBoxScheduleEntry>
)

@Serializable
internal data class ShiraBoxScheduleEntry(
    @SerialName("id")
    val id: Int,
    @SerialName("image")
    val image: String,
    @SerialName("name")
    val name: String,
    @SerialName("nextEpisodeNumber")
    val nextEpisodeNumber: Int,
    @SerialName("releaseRange")
    val releaseRange: List<String>,
    @SerialName("released")
    val released: Boolean,
    @SerialName("russianName")
    val russianName: String,
    @SerialName("shikimoriId")
    val shikimoriId: Int
)

@Serializable
internal data class ShiraBoxAnimeResponse(
    @SerialName("anime")
    val anime: ShiraBoxAnimeData
)

@Serializable
data class ShiraBoxAnimeData(
    @SerialName("name")
    val name: String,
    @SerialName("id")
    val id: Int,
    @SerialName("image")
    val image: String,
    @SerialName("russianName")
    val russianName: String,
    @SerialName("schedule")
    val schedule: Schedule,
    @SerialName("shikimoriId")
    val shikimoriId: Int
)

@Serializable
data class ScheduleData(
    @SerialName("nextEpisodeNumber")
    val nextEpisodeNumber: Int,
    @SerialName("releaseRange")
    val releaseRange: List<String>,
    @SerialName("released")
    val released: Boolean
)

@Serializable
data class Schedule(
    @SerialName("nextEpisodeNumber")
    val nextEpisodeNumber: Int,
    @SerialName("releaseRange")
    val releaseRange: List<String>,
    @SerialName("released")
    val released: Boolean
)