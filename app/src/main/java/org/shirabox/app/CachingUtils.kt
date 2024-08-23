package org.shirabox.app

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import org.shirabox.core.db.AppDatabase
import org.shirabox.core.model.ComplexContent
import org.shirabox.core.model.Content
import org.shirabox.core.model.ShiraBoxAnime
import org.shirabox.core.util.Util.Companion.mapContentToEntity
import org.shirabox.data.shirabox.ShiraBoxRepository

object CachingUtils {

    suspend fun cacheContent(db: AppDatabase, content: Content, update: Boolean) : ComplexContent {
        var anime = content
        var shiraBoxAnime: ShiraBoxAnime? = null

        // Fetch anime from ShiraBox API
        ShiraBoxRepository.fetchAnime(content.shikimoriId)
            .catch { it.printStackTrace() }
            .collectLatest { shiraBoxAnime = it }

        val cachedData = db.contentDao().getContent(content.shikimoriId)

        // Set shirabox API data
        anime = shiraBoxAnime?.let { anime.copy(shiraboxId = it.id, image = it.image) } ?: anime

        when(cachedData){
            null -> {
                val cachedContentId = db.contentDao().insertContents(
                    mapContentToEntity(
                        content = anime,
                        isFavourite = false,
                        lastViewTimestamp = System.currentTimeMillis(),
                        episodesNotifications = false,
                        pinnedTeams = emptyList()
                    )
                ).first()

                return ComplexContent(
                    content = db.contentDao().getContentByUid(cachedContentId),
                    shiraBoxAnime = shiraBoxAnime
                )
            }

            else -> {
                if(update) {
                    db.contentDao().updateContents(
                        mapContentToEntity(
                            contentUid = cachedData.uid,
                            content = anime,
                            isFavourite = cachedData.isFavourite,
                            lastViewTimestamp = cachedData.lastViewTimestamp,
                            episodesNotifications = cachedData.episodesNotifications,
                            pinnedTeams = cachedData.pinnedTeams
                        )
                    )
                }

                return ComplexContent(
                    content = db.contentDao().getContentByUid(cachedData.uid),
                    shiraBoxAnime = shiraBoxAnime
                )
            }
        }
    }
}