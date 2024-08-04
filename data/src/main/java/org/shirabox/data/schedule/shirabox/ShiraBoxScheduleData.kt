package org.shirabox.data.schedule.shirabox

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