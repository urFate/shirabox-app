package live.shirabox.data.content.anime.libria

import fuel.httpGet
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
        return search(query = query)
    }

    private suspend fun search(query: String): List<EpisodeEntity> {
        val response =
            "$url/v3/title".httpGet(listOf("code" to Util.encodeString(query))).also {
                if(it.statusCode != 200) return emptyList()
            }

        val data = json.decodeFromString<LibriaAnimeData>(response.body)
        val host = "https://${data.player.host}"

        return data.player.list.map { entry ->
            entry.value.let {
                EpisodeEntity(
                    name = it.name,
                    source = this.name,
                    episode = it.episode,
                    uploadTimestamp = it.createdTimestamp.toLong(),
                    videos = buildMap {
                        put(Quality.SD, host + it.hls.sd)
                        it.hls.hd?.let { url -> put(Quality.HD, host + url) }
                        it.hls.fhd?.let { url -> put(Quality.FHD, host + url) }
                    },
                    videoMarkers = Pair(
                        it.skips.opening.firstOrNull()?.times(1000L) ?: -1L,
                        it.skips.opening.lastOrNull()?.times(1000L) ?: -1L
                    ),
                    type = this.contentType
                )
            }
        }
    }
}