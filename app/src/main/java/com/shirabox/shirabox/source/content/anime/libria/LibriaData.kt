package com.shirabox.shirabox.source.content.anime.libria

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class LibriaSearchWrapper(
    val list: List<LibriaAnimeData>,
    val pagination: Pagination
)

@Serializable
data class Pagination(
    @SerialName("current_page") val currentPage: Int,
    @SerialName("items_per_page") val itemsPerPage: Int,
    val pages: Int,
    @SerialName("total_items") val totalItems: Int
)

@Serializable
data class LibriaAnimeData(
    val announce: String?,
    val code: String,
    val description: String,
    val genres: List<String>,
    val id: Int,
    val names: LibriaNames,
    val player: LibriaPlayer,
    val updated: Int
)

@Serializable
data class LibriaNames(
    val alternative: String?,
    val en: String,
    val ru: String
)

@Serializable
data class LibriaPlayer(
    @SerialName("alternative_player") val alternativePlayer: String?,
    val episodes: LibriaEpisodes,
    val host: String,
    val list: Map<String, LibriaEpisode>
)

@Serializable
data class LibriaEpisodes(
    val first: Int,
    val last: Int,
    val string: String
)

@Serializable
data class LibriaEpisode(
    @SerialName("created_timestamp") val createdTimestamp: Int,
    val episode: Int,
    val hls: LibriaVideo,
    val name: String?,
    val preview: String?,
    val skips: LibriaSkips,
    val uuid: String
)

@Serializable
data class LibriaVideo(
    val fhd: String?,
    val hd: String?,
    val sd: String
)

@Serializable
data class LibriaSkips(
    val ending: List<Int>,
    val opening: List<Int>
)

