package live.shirabox.data

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import live.shirabox.core.db.AppDatabase
import live.shirabox.core.entity.EpisodeEntity
import live.shirabox.core.model.Content
import live.shirabox.data.content.AbstractContentRepository

class EpisodesHelper(private val db: AppDatabase) {
    suspend fun completeEpisodesSearch(
        repository: AbstractContentRepository,
        content: Content,
        contentUid: Long,
        cachedEpisodes: List<EpisodeEntity>
    ) {
        repository.searchEpisodes(content).catch {
            it.printStackTrace()
            emitAll(emptyFlow())
        }.collectLatest {
            cacheEpisodes(it, cachedEpisodes, contentUid)
        }
    }

    suspend fun partialEpisodesSearch(
        repository: AbstractContentRepository,
        content: Content,
        contentUid: Long,
        cachedEpisodes: List<EpisodeEntity>,
        range: IntRange
    ) {
        repository.searchEpisodesInRange(content, range).catch {
            it.printStackTrace()
            emitAll(emptyFlow())
        }.collectLatest {
            cacheEpisodes(it, cachedEpisodes, contentUid)
        }
    }

    private fun cacheEpisodes(episodes: List<EpisodeEntity>, cachedEpisodes: List<EpisodeEntity>, contentUid: Long) {
        episodes.map { episodeEntity ->

            /**
             * Keep local data (e.g. watching time and id's)
             */

            when (
                val matchingEpisode =
                    cachedEpisodes.firstOrNull { it.episode == episodeEntity.episode && it.actingTeamName == episodeEntity.actingTeamName }
            ) {
                null -> episodeEntity.copy(uid = null, contentUid = contentUid)
                else -> {
                    episodeEntity.copy(
                        uid = matchingEpisode.uid,
                        contentUid = contentUid,
                        watchingTime = matchingEpisode.watchingTime,
                        readingPage = matchingEpisode.readingPage
                    )
                }
            }
        }.let { entities ->
            db.episodeDao().insertEpisodes(*entities.toTypedArray())
        }
    }
}