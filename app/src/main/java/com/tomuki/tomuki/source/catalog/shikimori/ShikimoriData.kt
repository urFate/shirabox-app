package com.tomuki.tomuki.source.catalog.shikimori

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Image(
    val preview: String, val original: String, val x96: String, val x48: String
)

@Serializable
data class LibraryAnimeData(
    val id: Int,
    val image: Image,
    val score: String,
    @SerialName("aired_on") val airedOn: String,
    val russian: String,
    @SerialName("episodes_aired") val episodesAired: Int,
    val kind: String,
    val name: String,
    val url: String,
    val episodes: Int,
    val status: String,
    @SerialName("released_on") val releasedOn: String
)

@Serializable
data class LibraryBookData(
    val image: Image,
    val score: Int,
    @SerialName("aired_on") val airedOn: String,
    val russian: String,
    val chapters: Int,
    val kind: String,
    val name: String,
    val volumes: Int,
    val id: Int,
    val url: String,
    val status: String,
    @SerialName("released_on") val releasedOn: String
)

@Serializable
data class AnimeData(
    val id: Int,
    val name: String,
    val russian: String,
    val image: Image,
    val url: String,
    val kind: String,
    val score: String,
    val status: String,
    val episodes: Int,
    @SerialName("episodes_aired") val episodesAired: Int,
    @SerialName("aired_on") val airedOn: String,
    @SerialName("released_on") val releasedOn: String,
    val rating: String,
    val english: List<String>,
    val japanese: List<String>,
    val synonyms: List<String>,
    val duration: Long,
    val description: String,
    val franchise: String,
    val favoured: Boolean,
    val anons: Boolean,
    val ongoing: Boolean,
    @SerialName("thread_id") val threadId: Long,
    @SerialName("topic_id") val topicId: Long,
    @SerialName("myanimelist_id") val myAnimeListId: Long,
    @SerialName("rates_scores_stats") val ratesScoresStats: List<RatesScoresStat>,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("next_episode_at") val nextEpisodeAt: String,
    val genres: List<Genre>,
    val studios: List<Studio>,
)

@Serializable
data class BookData(
    val id: Int,
    val name: String,
    val russian: String,
    val image: Image,
    val url: String,
    val kind: String,
    val score: String,
    val status: String,
    val volumes: Int,
    val chapters: Int,
    @SerialName("aired_on") val airedOn: String,
    @SerialName("released_on") val releasedOn: String,
    val english: List<String>,
    val japanese: List<String>,
    val synonyms: List<String>,
    val description: String,
    val franchise: String,
    val favoured: Boolean,
    val anons: Boolean,
    val ongoing: Boolean,
    @SerialName("thread_id") val threadId: Long,
    @SerialName("topic_id") val topicId: Long,
    @SerialName("myanimelist_id") val myAnimeListId: Long,
    @SerialName("rates_scores_stats") val ratesScoresStats: List<RatesScoresStat>,
    val genres: List<Genre>,
    val publishers: List<Publisher>,
)

@Serializable
data class LibraryAnimeInitiator(
    val libraryAnimeData: List<LibraryAnimeData>
)

@Serializable
data class LibraryBookInitiator(
    val libraryBookData: List<LibraryBookData>
)

@Serializable
data class RatesScoresStat(
    val name: Int,
    val value: Int,
)

@Serializable
data class Genre(
    val id: Int, val name: String, val russian: String
)

@Serializable
data class Studio(
    val id: Int,
    val name: String,
    @SerialName("filtered_name") val filteredName: String,
    val real: Boolean,
    val image: String,
)

@Serializable
data class Publisher(
    val id: Int,
    val name: String,
)