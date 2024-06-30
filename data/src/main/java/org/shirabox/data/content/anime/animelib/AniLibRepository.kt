package org.shirabox.data.content.anime.animelib

import fuel.httpGet
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.serialization.SerializationException
import org.shirabox.core.entity.EpisodeEntity
import org.shirabox.core.model.Content
import org.shirabox.core.model.ContentType
import org.shirabox.core.model.Quality
import org.shirabox.data.content.AbstractContentRepository
import java.text.SimpleDateFormat
import java.util.Locale

class AniLibRepository : AbstractContentRepository(
    name = "AniLib",
    url = "https://anilib.me",
    contentType = ContentType.ANIME
) {
    companion object {
        private const val API_ENDPOINT = "https://api.lib.social/api"
        private const val VIDEO_HOST_ENDPOINT = "https://video1.anilib.me/.%D0%B0s/"
        private const val REQUEST_DELAY_MS = 150L
        private const val UNLIMITED_REQUESTS_THRESHOLD = 99
        private const val ON_RATE_LIMIT_WAIT_DELAY = 60000L
    }

    override suspend fun searchEpisodes(content: Content): Flow<List<EpisodeEntity>> =
        searchEpisodesInRange(content, 0..Int.MAX_VALUE)

    override suspend fun searchEpisodesInRange(
        content: Content,
        range: IntRange
    ): Flow<List<EpisodeEntity>> = flow {
        try {
            val slugUrl = searchContentSlugURL(content).catch {
                it.printStackTrace()
                emitAll(emptyFlow())
            }.singleOrNull()

            val episodesHeaders = fetchEpisodesHeaders(slugUrl!!).catch {
                it.printStackTrace()
                emitAll(emptyFlow())
            }.singleOrNull()?.filter { it.number.toInt() in range }

            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())

            episodesHeaders!!.forEach { headers ->

                /**
                 * Retry request with 2 sec delay when NullPointerException caught.
                 * Usually throws when server's rate limit is reached
                 */

                val episode = fetchEpisode(headers.id)
                    .retryWhen { cause, _ ->
                        cause.printStackTrace()
                        when (cause) {
                            is SerializationException -> {
                                delay(ON_RATE_LIMIT_WAIT_DELAY)
                                true
                            }

                            else -> false
                        }
                    }.catch {
                        it.printStackTrace()
                        emitAll(emptyFlow())
                    }.singleOrNull()

                val number  = headers.number.toInt()
                val name = headers.name

                /**
                 * Exclude kodik and subtitles only players and duplicated players
                 * (by unknown reason ._.) with the same acting team
                 */

                val filteredPlayers =
                    episode!!.players.filter { it.video != null && it.translationType.id == 2 }
                        .distinctBy { it.team }

                val entities = filteredPlayers.map { player ->
                    val createdAt = formatter.parse(player.createdAt)?.time ?: System.currentTimeMillis()
                    val streams = player.video!!.quality
                    val logoUrl = when(player.team.cover.filename) {
                        null -> ""
                        else -> "$url${player.team.cover.thumbnail}"
                    }

                    EpisodeEntity(
                        source = this@AniLibRepository.name,
                        actingTeamName = player.team.name,
                        actingTeamIcon = logoUrl,
                        name = name,
                        episode = number,
                        uploadTimestamp = createdAt,
                        videos = streams.associate {
                            Quality.valueOfInt(it.quality) to VIDEO_HOST_ENDPOINT+it.href
                        },
                        type = ContentType.ANIME
                    )
                }

                emit(entities)

                if(episodesHeaders.size > UNLIMITED_REQUESTS_THRESHOLD) delay(REQUEST_DELAY_MS)
            }
        } catch (ex: Exception) { throw ex }
    }


    private fun fetchEpisode(id: Int): Flow<EpisodeObject> = flow {
        try {
            val response = "$API_ENDPOINT/episodes/$id".httpGet().body
            val jsonData = json.decodeFromString<AniLibWrapper<EpisodeObject>>(response)
            emit(jsonData.data)
        } catch (ex: Exception) { throw ex }
    }

    private fun fetchEpisodesHeaders(slugUrl: String): Flow<List<EpisodeHeaders>> = flow {
        try {
            val response = "$API_ENDPOINT/episodes".httpGet(listOf("anime_id" to slugUrl)).body
            val jsonData = json.decodeFromString<AniLibWrapper<List<EpisodeHeaders>>>(response)

            emit(jsonData.data)
        } catch (ex: Exception) { throw ex }
    }

    private suspend fun searchContentSlugURL(content: Content): Flow<String> = flow {
        try {
            val response =
                "$API_ENDPOINT/anime?fields[]=rate_avg&fields[]=rate&fields[]=releaseDate".httpGet(
                    listOf("q" to content.enName)
                ).body
            val jsonData = json.decodeFromString<AniLibWrapper<List<SearchObject>>>(response).data

            emit(jsonData[0].slugUrl)
        } catch (ex: Exception) { throw ex }
    }
}