package live.shirabox.data.content.anime.animelib

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class AniLibWrapper <T>(
    @SerialName("data")
    val data: T
)

@Serializable
internal data class SearchObject(
    @SerialName("slug_url")
    val slugUrl: String
)

@Serializable
data class EpisodeHeaders(
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("id")
    val id: Int,
    @SerialName("item_number")
    val itemNumber: Int,
    @SerialName("name")
    val name: String,
    @SerialName("number")
    val number: String,
    @SerialName("number_secondary")
    val numberSecondary: String,
)

@Serializable
data class EpisodeObject(
    @SerialName("name")
    val name: String,
    @SerialName("number")
    val number: String,
    @SerialName("players")
    val players: List<Player>
)

@Serializable
data class Player(
    @SerialName("player")
    val player: String,
    @SerialName("subtitles")
    val subtitles: List<Subtitle>,
    @SerialName("team")
    val team: Team,
    @SerialName("translation_type")
    val translationType: TranslationType,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("video")
    val video: Video? = null
)

@Serializable
data class Subtitle(
    @SerialName("filename")
    val filename: String,
    @SerialName("format")
    val format: String,
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("src")
    val src: String
)

@Serializable
data class Team(
    @SerialName("cover")
    val cover: Cover,
    @SerialName("id")
    val id: Int,
    @SerialName("model")
    val model: String,
    @SerialName("name")
    val name: String,
    @SerialName("slug")
    val slug: String,
    @SerialName("slug_url")
    val slugUrl: String,
    @SerialName("stats")
    val stats: List<Stat>,
)

@Serializable
data class TranslationType(
    @SerialName("id")
    val id: Int,
    @SerialName("label")
    val label: String
)

@Serializable
data class Video(
    @SerialName("id")
    val id: Int,
    @SerialName("quality")
    val quality: List<Quality>
)

@Serializable
data class Cover(
    @SerialName("filename")
    val filename: String? = null,
    @SerialName("default")
    val default: String,
    @SerialName("thumbnail")
    val thumbnail: String
)

@Serializable
data class Stat(
    @SerialName("formated")
    val formated: String,
    @SerialName("label")
    val label: String,
    @SerialName("short")
    val short: String,
    @SerialName("tag")
    val tag: String,
    @SerialName("value")
    val value: Int
)

@Serializable
data class Quality(
    @SerialName("bitrate")
    val bitrate: Int,
    @SerialName("href")
    val href: String,
    @SerialName("quality")
    val quality: Int
)

