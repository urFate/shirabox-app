package org.shirabox.data.content.anime.libria

import fuel.httpGet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.shirabox.core.entity.EpisodeEntity
import org.shirabox.core.model.Content
import org.shirabox.core.model.ContentKind
import org.shirabox.core.model.ContentType
import org.shirabox.core.model.Quality
import org.shirabox.core.model.ReleaseStatus
import org.shirabox.data.content.AbstractContentRepository
import java.text.SimpleDateFormat
import java.util.Locale


class LibriaRepository : AbstractContentRepository(
    "Libria",
    "https://anilibria.top",
    ContentType.ANIME,
) {
    companion object {
        const val API_ENDPOINT = "https://anilibria.top/api"
        const val ACTING_TEAM_LOGO_URL = "https://anilibria.top/static/apple-touch-icon.png"
    }

    override suspend fun searchEpisodes(content: Content): Flow<List<EpisodeEntity>> = flow {
        emit(lookupEpisodes(content))
    }

    override suspend fun searchEpisodesInRange(
        content: Content,
        range: IntRange
    ): Flow<List<EpisodeEntity>> = flow {
        val results = lookupEpisodes(content)

        emit(
            results.subList(
                range.first.coerceIn(0, results.size),
                range.last.coerceIn(0, results.size)
            )
        )
    }

    private suspend fun lookupEpisodes(content: Content): List<EpisodeEntity> {
        try {
            val id = search(content)?.id
            val response = "$API_ENDPOINT/v1/anime/releases/$id"
                .httpGet()
                .also { if (it.statusCode != 200) return emptyList() }

            return json.decodeFromString<LibriaAnimeItem>(response.body).episodes.map(::mapEpisode)
        } catch (ex: Exception) {
            ex.printStackTrace()
            return emptyList()
        }
    }

    private suspend fun search(content: Content): LibriaSearchItem? {
        try {
            val response = "$API_ENDPOINT/v1/app/search/releases"
                .httpGet(listOf("query" to content.enName))
                .also {
                    if (it.statusCode != 200) return null
                }

            val isContentOngoing = content.status == ReleaseStatus.RELEASING

            val data = json.decodeFromString<List<LibriaSearchItem>>(response.body)
                .firstOrNull {
                    it.year.toString() == content.releaseYear
                            && it.isOngoing == isContentOngoing
                            && decodeKind(it.type) == content.kind
                }

            return data
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
    }

    private fun mapEpisode(data: LibriaEpisode): EpisodeEntity {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ", Locale.getDefault())

        return EpisodeEntity(
            name = data.name,
            source = this.name,
            episode = data.sortOrder,
            uploadTimestamp = formatter.parse(data.updatedAt)?.time ?: System.currentTimeMillis(),
            videos = buildMap {
                data.hls480?.let { put(Quality.SD, it) }
                data.hls720?.let { put(Quality.HD, it) }
                data.hls1080?.let { put(Quality.FHD, it) }
            },
            videoMarkers = Pair(
                data.opening.start?.times(1000L) ?: -1L,
                data.opening.stop?.times(1000L) ?: -1L
            ),
            actingTeamName = "AniLibria",
            actingTeamIcon = ACTING_TEAM_LOGO_URL,
            type = this.contentType
        )
    }

    private fun decodeKind(libriaType: LibriaType): ContentKind = when(libriaType.value) {
        "TV" -> ContentKind.TV
        "ONA" -> ContentKind.ONA
        "OVA", "OAD", "WEB" -> ContentKind.OVA
        "MOVIE" -> ContentKind.MOVIE
        "SPECIAL" -> ContentKind.SPECIAL
        else -> ContentKind.OTHER
    }
}