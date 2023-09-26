package com.shirabox.shirabox.source.content.anime.libria

import android.net.Uri
import com.shirabox.shirabox.db.entity.EpisodeEntity
import com.shirabox.shirabox.model.ContentType
import com.shirabox.shirabox.model.EpisodesInfo
import com.shirabox.shirabox.model.Quality
import com.shirabox.shirabox.source.content.AbstractContentSource

object AniLibria : AbstractContentSource (
    "AniLibria",
    "https://api.anilibria.tv",
    ContentType.ANIME,
    "https://anilibria.tv/favicons/apple-touch-icon.png"
) {
    override suspend fun searchEpisodes(query: String): List<EpisodeEntity> {
        return search(query = query)
    }

    override suspend fun searchEpisodesInfo(query: String): EpisodesInfo? {
        val data = search(query).lastOrNull()

        return data?.let {
            EpisodesInfo(
                episodes = it.episode,
                lastEpisodeTimestamp = it.uploadTimestamp
            )
        }
    }

    private fun search(query: String): List<EpisodeEntity> {
        val response = httpGET("$url/v3/title/search?search=${Uri.encode(query)}&limit=1")
            ?: return emptyList()
        val data = json.decodeFromString<LibriaSearchWrapper>(response).list

        val player = data.firstOrNull()?.player
        val host = "https://${player?.host}"

        return player?.list?.map { entry ->
            entry.value.let {
                EpisodeEntity(
                    name = it.name,
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
        } ?: emptyList()
    }
}