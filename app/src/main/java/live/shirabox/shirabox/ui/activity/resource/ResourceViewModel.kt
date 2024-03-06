package live.shirabox.shirabox.ui.activity.resource

import android.content.Context
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import live.shirabox.core.entity.EpisodeEntity
import live.shirabox.core.entity.RelatedContentEntity
import live.shirabox.core.model.Content
import live.shirabox.core.model.ContentType
import live.shirabox.core.util.Util.Companion.mapContentToEntity
import live.shirabox.core.util.Util.Companion.mapEntityToContent
import live.shirabox.data.DataSources
import live.shirabox.data.catalog.shikimori.Shikimori
import live.shirabox.data.content.AbstractContentSource
import live.shirabox.shirabox.db.AppDatabase

class ResourceViewModel(context: Context, private val contentType: ContentType) : ViewModel() {
    val content = mutableStateOf<Content?>(null)
    val related = mutableStateListOf<Content>()

    val databaseUid = mutableIntStateOf(-1)

    val isFavourite = mutableStateOf(false)
    val pinnedSources = mutableStateListOf<String>()

    val episodeFetchComplete = mutableStateOf(false)

    private val db = AppDatabase.getAppDataBase(context)

    val sources = DataSources.contentSources.filter { it.contentType == contentType }

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
                data.let {
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
                    RelatedContentEntity(
                        contentUid = id,
                        shikimoriID = it.shikimoriID
                    )
                }.toTypedArray()
            )
        }
    }

    fun fetchCachedEpisodes():
            Flow<List<EpisodeEntity>> = db?.episodeDao()?.all() ?: emptyFlow()

    fun fetchEpisodes(id: Int, query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val finishedDeferred = async {
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
                true
            }

            episodeFetchComplete.value = finishedDeferred.await()
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