package com.shirabox.shirabox.source.content.anime.libria

import android.net.Uri
import com.shirabox.shirabox.model.ContentType
import com.shirabox.shirabox.model.Episode
import com.shirabox.shirabox.model.EpisodesInfo
import com.shirabox.shirabox.model.Quality
import com.shirabox.shirabox.source.content.AbstractContentSource

object AniLibria : AbstractContentSource (
    "AniLibria",
    "https://api.anilibria.tv",
    ContentType.ANIME,
    "https://anilibria.tv/favicons/apple-touch-icon.png"
) {
    override suspend fun searchEpisodes(query: String, videoQuality: Quality?): List<Episode> {
        return search(query = query, videoQuality = videoQuality)
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

    private fun search(query: String, videoQuality: Quality? = null): List<Episode> {
        val response = httpGET("$url/v3/title/search?search=${Uri.encode(query)}&limit=1")
            ?: return emptyList()
        val data = json.decodeFromString<LibriaSearchWrapper>(response).list

        return data.firstOrNull()?.player?.list?.map { entry ->
            entry.value.let {
                Episode(
                    name = it.name,
                    episode = it.episode,
                    uploadTimestamp = it.createdTimestamp.toLong(),
                    contents = listOf(
                        when(videoQuality) {
                            Quality.SD -> it.hls.sd
                            Quality.HD -> it.hls.hd ?: it.hls.sd
                            Quality.FHD -> it.hls.fhd ?: it.hls.hd ?: it.hls.sd
                            else -> ""
                        }
                    ),
                    videoMarkers = Pair(
                        it.skips.opening.lastOrNull()?.times(1000L),
                        it.skips.ending.lastOrNull()?.times(1000L)
                    ),
                    type = this.contentType
                )
            }
        } ?: emptyList()
    }
}