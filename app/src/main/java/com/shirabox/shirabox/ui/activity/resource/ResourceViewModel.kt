package com.shirabox.shirabox.ui.activity.resource

import android.content.Context
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shirabox.shirabox.db.AppDatabase
import com.shirabox.shirabox.db.entity.EpisodeEntity
import com.shirabox.shirabox.db.entity.RelatedContentEntity
import com.shirabox.shirabox.model.Content
import com.shirabox.shirabox.model.ContentType
import com.shirabox.shirabox.source.catalog.shikimori.Shikimori
import com.shirabox.shirabox.source.content.AbstractContentSource
import com.shirabox.shirabox.source.content.anime.libria.AniLibria
import com.shirabox.shirabox.source.content.manga.remanga.Remanga
import com.shirabox.shirabox.util.Util.Companion.mapContentToEntity
import com.shirabox.shirabox.util.Util.Companion.mapEntityToContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

class ResourceViewModel(context: Context, private val contentType: ContentType) : ViewModel() {
    val content = mutableStateOf<Content?>(null)
    val related = mutableStateListOf<Content>()

    val databaseUid = mutableIntStateOf(-1)

    val isFavourite = mutableStateOf(false)
    val pinnedSources = mutableStateListOf<String>()

    val isTimeout = mutableStateOf(false)

    val db = AppDatabase.getAppDataBase(context)

    val sources = listOf(
        AniLibria, Remanga
    ).filter { it.contentType == contentType }

    fun fetchContent(shikimoriId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val pickedData = db?.contentDao()?.collectedContent(shikimoriId)

            pickedData?.let {
                content.value = mapEntityToContent(it.content)
                isFavourite.value = it.content.isFavourite
                databaseUid.intValue = it.content.uid
                pinnedSources.addAll(it.content.pinnedSources)

                return@launch
            }

            val data = Shikimori.fetchContent(shikimoriId, contentType)

            db?.let { database ->
                data?.let {
                    database.contentDao().insertContents(
                        mapContentToEntity(
                            content = it,
                            isFavourite = false,
                            lastViewTimestamp = System.currentTimeMillis(),
                            pinnedSources = emptyList()
                        )
                    )
                }

                databaseUid.intValue = database.contentDao().getContent(shikimoriId).uid
                content.value = data
            }
        }
    }

    fun fetchRelated(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            Shikimori.fetchRelated(id, contentType).forEach { it?.let(related::add) }

            db?.relatedDao()?.insertRelated(
                *related.map {
                    RelatedContentEntity(contentUid = id, shikimoriID = it.shikimoriID)
                }.toTypedArray()
            )
        }
    }

    fun fetchCachedEpisodes():
            Flow<List<EpisodeEntity>> = db?.episodeDao()?.all() ?: emptyFlow()

    fun fetchEpisodes(id: Int, query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db?.contentDao()?.collectedContent(id)?.let { collectedContent ->
                sources.forEach { source ->
                    source.searchEpisodes(query).let { list ->
                        list.mapIndexed { index, episodeEntity ->
                            val matchingEpisode = collectedContent.episodes.getOrNull(index)

                            /**
                             * Keep local data (e.g. watching time and id's)
                             */

                            episodeEntity.copy(
                                uid = matchingEpisode?.uid,
                                contentUid = collectedContent.content.uid,
                                watchingTime = matchingEpisode?.watchingTime ?: -1L,
                                readingPage = matchingEpisode?.readingPage ?: -1
                            )
                        }.toTypedArray().let { entities ->
                            db.episodeDao().insertEpisodes(*entities)
                        }
                    }
                }
            }
        }
    }

    fun switchFavouriteStatus(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            isFavourite.value = !isFavourite.value

            val content = db?.contentDao()?.getContent(id)
            content?.let {
                db?.contentDao()?.updateContents(it.copy(isFavourite = isFavourite.value))
            }
        }
    }

    fun switchSourcePinStatus(id: Int, source: AbstractContentSource) {
        viewModelScope.launch(Dispatchers.IO) {
            val content = db?.contentDao()?.getContent(id)
            content?.let {
                if (pinnedSources.contains(source.name)) pinnedSources.remove(source.name) else pinnedSources.add(
                    source.name
                )

                db?.contentDao()?.updateContents(it.copy(pinnedSources = pinnedSources))
            }
        }
    }
}