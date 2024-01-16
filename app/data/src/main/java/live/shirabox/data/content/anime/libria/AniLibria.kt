package live.shirabox.data.content.anime.libria

import fuel.httpGet
import live.shirabox.core.entity.EpisodeEntity
import live.shirabox.core.model.ContentType
import live.shirabox.core.model.Quality
import live.shirabox.data.content.AbstractContentSource


class AniLibria : AbstractContentSource(
    "AniLibria",
    "https://api.anilibria.tv",
    ContentType.ANIME,
    "https://anilibria.tv/favicons/apple-touch-icon.png"
) {
    override suspend fun searchEpisodes(query: String): List<EpisodeEntity> {
        return search(query = query)
    }

    private suspend fun search(query: String): List<EpisodeEntity> {
        val response =
            "$url/v3/title".httpGet(listOf("code" to query))

        var data: LibriaAnimeData?

        return when(response.statusCode) {
            200 -> {
                data = json.decodeFromString<LibriaAnimeData>(response.body)

                data.player.list.map { entry ->
                    mapEpisode(entry.value, data!!.player.host)
                }
            }

            /**
             * Sometimes codes is shortened or just does not match by unknown reason
             * In this case we try fetch release via search
             */

            404 -> {
                val retryResponse = "$url/v3/title/search"
                    .httpGet(listOf("search" to query)).also {
                        if (it.statusCode != 200) return emptyList()
                    }
                data = json.decodeFromString<LibriaSearchWrapper>(retryResponse.body).list.firstOrNull()

                data?.let {
                    data.player.list.map { entry ->
                        mapEpisode(entry.value, data.player.host)
                    }
                } ?: emptyList()
            }

            else -> emptyList()
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