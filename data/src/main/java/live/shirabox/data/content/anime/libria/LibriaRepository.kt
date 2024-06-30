package live.shirabox.data.content.anime.libria

import fuel.httpGet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import live.shirabox.core.entity.EpisodeEntity
import live.shirabox.core.model.Content
import live.shirabox.core.model.ContentKind
import live.shirabox.core.model.ContentType
import live.shirabox.core.model.Quality
import live.shirabox.data.content.AbstractContentRepository
import java.net.SocketTimeoutException


class LibriaRepository : AbstractContentRepository(
    "Libria",
    "https://anilibria.tv",
    ContentType.ANIME,
) {
    companion object {
        const val API_ENDPOINT = "https://api.anilibria.tv"
        const val ACTING_TEAM_LOGO_URL = "https://anilibria.tv/favicons/apple-touch-icon.png"
    }

    override suspend fun searchEpisodes(content: Content): Flow<List<EpisodeEntity>> = flow {
        emit(advancedSearch(content))
    }

    override suspend fun searchEpisodesInRange(
        content: Content,
        range: IntRange
    ): Flow<List<EpisodeEntity>> = flow {
        val results = advancedSearch(content)

        emit(
            results.subList(
                range.first.coerceIn(0, results.size),
                range.last.coerceIn(0, results.size)
            )
        )
    }

    private suspend fun advancedSearch(content: Content): List<EpisodeEntity> {
        val altNamesListQuery = "(${content.altNames.joinToString { "\"${it}\"" }})"
        if (libriaKind(content.kind) == null) return emptyList()

        try {
            val response = "$API_ENDPOINT/v3/title/search/advanced"
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
            uploadTimestamp = data.createdTimestamp.toLong().times(1000L),
            videos = buildMap {
                put(Quality.SD, hostUrl + data.hls.sd)
                data.hls.hd?.let { url -> put(Quality.HD, hostUrl + url) }
                data.hls.fhd?.let { url -> put(Quality.FHD, hostUrl + url) }
            },
            videoMarkers = Pair(
                data.skips.opening.firstOrNull()?.times(1000L) ?: -1L,
                data.skips.opening.lastOrNull()?.times(1000L) ?: -1L
            ),
            actingTeamName = "AniLibria",
            actingTeamIcon = ACTING_TEAM_LOGO_URL,
            type = this.contentType
        )
    }

    private fun libriaKind(kind: ContentKind) : Int? = when(kind) {
        ContentKind.MOVIE -> 0
        ContentKind.TV -> 1
        ContentKind.OVA -> 2
        ContentKind.ONA -> 3
        ContentKind.SPECIAL -> 4
        else -> null
    }
}