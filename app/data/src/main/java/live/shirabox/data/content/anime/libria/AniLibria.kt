package live.shirabox.data.content.anime.libria

import fuel.FuelBuilder
import fuel.Request
import fuel.httpGet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import live.shirabox.core.entity.EpisodeEntity
import live.shirabox.core.model.ContentType
import live.shirabox.core.model.Quality
import live.shirabox.core.util.Util
import live.shirabox.data.content.AbstractContentSource


class AniLibria : AbstractContentSource(
    "AniLibria",
    "https://api.anilibria.tv",
    ContentType.ANIME,
    "https://anilibria.tv/favicons/apple-touch-icon.png"
) {
    override suspend fun searchEpisodes(query: String): List<EpisodeEntity> {
        return smartSearch(query = query)
    }

    private suspend fun smartSearch(query: String): List<EpisodeEntity> {
        return withContext(Dispatchers.IO) {
            val codeSearchDeferred = async { codeSearch(Util.encodeString(query)) }
            val classicSearchDeferred = async { classicSearch(query) }

            val classicSearchResult = classicSearchDeferred.await()

            return@withContext codeSearchDeferred.await() ?: classicSearchResult
        }
    }

    private suspend fun classicSearch(query: String): List<EpisodeEntity> {
        val data: LibriaAnimeData?

        val retryResponse = "$url/v3/title/search"
            .httpGet(listOf("search" to query, "playlist_type" to "array")).also {
                if (it.statusCode != 200) return emptyList()
            }
        data = json.decodeFromString<LibriaSearchWrapper>(retryResponse.body).list.firstOrNull()

        return data?.let {
            data.player.list.map { entry ->
                mapEpisode(entry, data.player.host)
            }
        } ?: emptyList()
    }

    private suspend fun codeSearch(code: String): List<EpisodeEntity>? {
        val request = Request.Builder()
            .url("$url/v3/title")
            .parameters(
                listOf(
                    "code" to code,
                    "playlist_type" to "array"
                )
            )
            .build()

        val fuel = FuelBuilder().config(myClient).build()
        val response = fuel.get(request)

        return when (response.statusCode) {
            200 -> {
                val data = json.decodeFromString<LibriaAnimeData>(response.body)
                data.player.list.map { entry ->
                    mapEpisode(entry, data.player.host)
                }
            }
            else -> null
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
}