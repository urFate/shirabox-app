package org.shirabox.data.content.anime.libria

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class LibriaSearchItem(
    @SerialName("id")
    val id: Int,
    @SerialName("type")
    val type: LibriaType,
    @SerialName("year")
    val year: Int,
    @SerialName("is_ongoing")
    val isOngoing: Boolean
)

@Serializable
data class LibriaAnimeItem(
    @SerialName("episodes")
    val episodes: List<LibriaEpisode>
)

@Serializable
data class LibriaEpisode(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String?,
    @SerialName("name_english")
    val nameEnglish: String?,
    @SerialName("sort_order")
    val sortOrder: Int,
    @SerialName("opening")
    val opening: LibriaOpening,
    @SerialName("hls_1080")
    val hls1080: String?,
    @SerialName("hls_720")
    val hls720: String?,
    @SerialName("hls_480")
    val hls480: String?,
    @SerialName("updated_at")
    val updatedAt: String
)

@Serializable
data class LibriaOpening(
    @SerialName("start")
    val start: Int?,
    @SerialName("stop")
    val stop: Int?
)

@Serializable
data class LibriaType(
    @SerialName("value")
    val value: String?
)