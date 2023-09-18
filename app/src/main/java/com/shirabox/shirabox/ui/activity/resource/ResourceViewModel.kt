package com.shirabox.shirabox.ui.activity.resource

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shirabox.shirabox.db.AppDatabase
import com.shirabox.shirabox.db.entity.EpisodeEntity
import com.shirabox.shirabox.db.entity.RelatedContentEntity
import com.shirabox.shirabox.model.Content
import com.shirabox.shirabox.model.ContentType
import com.shirabox.shirabox.model.EpisodesInfo
import com.shirabox.shirabox.source.catalog.shikimori.Shikimori
import com.shirabox.shirabox.source.content.AbstractContentSource
import com.shirabox.shirabox.source.content.anime.libria.AniLibria
import com.shirabox.shirabox.source.content.manga.remanga.Remanga
import com.shirabox.shirabox.util.Util.Companion.mapContentToEntity
import com.shirabox.shirabox.util.Util.Companion.mapEntityToContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ResourceViewModel(context: Context, val contentType: ContentType) : ViewModel() {
    val content = mutableStateOf<Content?>(null)
    val related = mutableStateListOf<Content>()
    val episodes = mutableStateMapOf<AbstractContentSource, List<EpisodeEntity>>()
    val episodesInfo = mutableStateMapOf<AbstractContentSource, EpisodesInfo?>()

    val isFavourite = mutableStateOf(false)
    val pinnedSources = mutableStateListOf<String>()

    val db = AppDatabase.getAppDataBase(context)

    val sources = listOf(
        AniLibria, Remanga
    ).filter { it.contentType == contentType }

    fun fetchContent(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val pickedData = db?.contentDao()?.collectedContent(id)

            pickedData?.let {
                content.value = mapEntityToContent(it.content)
                isFavourite.value = it.content.isFavourite
                pinnedSources.addAll(it.content.pinnedSources)

                return@launch
            }

            val data = Shikimori.fetchContent(id, contentType)
            content.value = data

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

    fun fetchEpisodes(id: Int, query: String, source: AbstractContentSource) {
        viewModelScope.launch(Dispatchers.IO) {
            db?.contentDao()?.collectedContent(id)?.let { collectedContent ->
                val cachedEpisodes = collectedContent.episodes

                episodes[source] = cachedEpisodes

                source.searchEpisodes(query).let { list ->
                    if (list.size != episodes[source]?.size) {
                        episodes[source] = list

                        episodes[source]?.map {
                            it.copy(contentUid = collectedContent.content.uid)
                        }?.toTypedArray()?.let { db.episodeDao().insertEpisodes(*it) }
                    }
                }
            }
        }
    }

    fun fetchEpisodesInfo(id: Int, query: String, source: AbstractContentSource) {
        viewModelScope.launch(Dispatchers.IO) {
            db?.contentDao()?.collectedContent(id)?.episodes?.let { episodeEntities ->
                if (episodeEntities.isNotEmpty()) {
                    episodesInfo[source] = EpisodesInfo(
                        episodes = episodeEntities.size,
                        lastEpisodeTimestamp = episodeEntities.maxOfOrNull { it.uploadTimestamp }
                            ?: 0
                    )
                }

                source.searchEpisodesInfo(query)?.let {
                    episodesInfo[source] = it
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