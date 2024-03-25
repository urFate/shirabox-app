package live.shirabox.data.content.anime.libria

import fuel.httpGet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import live.shirabox.core.entity.EpisodeEntity
import live.shirabox.core.model.Content
import live.shirabox.core.model.ContentType
import live.shirabox.core.model.Quality
import live.shirabox.data.content.AbstractContentRepository
import java.net.SocketTimeoutException


class LibriaRepository : AbstractContentRepository(
    "AniLibria",
    "https://api.anilibria.tv",
    ContentType.ANIME,
    "https://anilibria.tv/favicons/apple-touch-icon.png"
) {
    override suspend fun searchEpisodes(content: Content): Flow<List<EpisodeEntity>> {
        return flow { emit(advancedSearch(content)) }
    }

    private suspend fun advancedSearch(content: Content): List<EpisodeEntity> {
        val altNamesListQuery = "(${content.altNames.joinToString { "\"${it}\"" }})"
        if(libriaKind(content.kind) == null) return emptyList()

        try {
            val response = "$url/v3/title/search/advanced"
                .httpGet(listOf(
                    "query" to "${content.releaseYear?.let { "{season.year} == $it" }} " +
                            "and {type.code} == ${libriaKind(content.kind)} and " +
                            "({names.en} == \"${content.enName}\" or " +
                            "{names.ru} == \"${content.name}\"" +
                            (if(content.altNames.isNotEmpty()) " or {names.en} in $altNamesListQuery or {names.ru} in $altNamesListQuery" else "") +
                            ")",
                    "playlist_type" to "array"
                )).also {
                    if (it.statusCode != 200) return emptyList()
                }
            val data = json.decodeFromString<LibriaSearchWrapper>(response.body).list.firstOrNull()

            return data?.let {
                data.player.list.map { entry ->
                    mapEpisode(entry, data.player.host)
                }
            } ?: emptyList()
        } catch (_: SocketTimeoutException) {
            return emptyList()
        }
    }

    private fun mapEpisode(data: LibriaEpisode, host: String) : EpisodeEntity {
        val hostUrl = "https://$host"

        return EpisodeEntity(
            name = data.name,
            source = this.name,
            episode = data.episode,
            uploadTimestamp = data.createdTimestamp.toLong(),
            videos = buildMap {
                put(Quality.SD, hostUrl + data.hls.sd)
                data.hls.hd?.let { url -> put(Quality.HD, hostUrl + url) }
                data.hls.fhd?.let { url -> put(Quality.FHD, hostUrl + url) }
            },
            videoMarkers = Pair(
                data.skips.opening.firstOrNull()?.times(1000L) ?: -1L,
                data.skips.opening.lastOrNull()?.times(1000L) ?: -1L
            ),
            type = this.contentType
        )
    }

    private fun libriaKind(kind: String): Int? {
        return when(kind) {
            "Фильм" -> 0
            "Сериал" -> 1
            "OVA" -> 2
            "ONA" -> 3
            "Спешл" -> 4
            else -> null
        }
    }
}