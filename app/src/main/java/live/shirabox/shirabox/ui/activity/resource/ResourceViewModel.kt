package live.shirabox.shirabox.ui.activity.resource

import android.content.Context
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import live.shirabox.core.datastore.AppDataStore
import live.shirabox.core.datastore.DataStoreScheme
import live.shirabox.core.entity.EpisodeEntity
import live.shirabox.core.model.Content
import live.shirabox.core.model.ContentType
import live.shirabox.core.util.Util
import live.shirabox.core.util.Util.Companion.mapContentToEntity
import live.shirabox.core.util.Util.Companion.mapEntityToContent
import live.shirabox.data.DataSources
import live.shirabox.data.catalog.shikimori.ShikimoriRepository
import live.shirabox.data.content.AbstractContentRepository
import live.shirabox.shirabox.db.AppDatabase

class ResourceViewModel(context: Context, private val contentType: ContentType) : ViewModel() {
    private val db = AppDatabase.getAppDataBase(context)

    val content = mutableStateOf<Content?>(null)
    val relatedContents = mutableStateListOf<Content>()

    val internalContentUid = mutableLongStateOf(0)

    val isFavourite = mutableStateOf(false)
    val pinnedSources = mutableStateListOf<String>()

    val episodeFetchComplete = mutableStateOf(false)
    val contentObservationException = mutableStateOf<Exception?>(null)

    val repositories = DataSources.contentSources.filter { it.contentType == contentType }

    fun fetchContent(shikimoriId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            db?.let { database ->
                val pickedData = database.contentDao().getContent(shikimoriId)

                pickedData?.let {
                    content.value = mapEntityToContent(it)
                    isFavourite.value = it.isFavourite
                    internalContentUid.longValue = it.uid
                    pinnedSources.addAll(it.pinnedSources)

                    return@launch
                }

                ShikimoriRepository.fetchContent(shikimoriId, contentType).catch {
                    contentObservationException.value = it as Exception
                    it.printStackTrace()
                    emitAll(emptyFlow())
                }.collect {
                    val newUid = database.contentDao().insertContents(
                        mapContentToEntity(
                            content = it,
                            isFavourite = false,
                            lastViewTimestamp = System.currentTimeMillis(),
                            pinnedSources = emptyList()
                        )
                    ).first()

                    internalContentUid.longValue = newUid
                    content.value = it
                }
            }
        }
    }

    fun fetchRelated(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            ShikimoriRepository.fetchRelated(id, contentType).catch {
                it.printStackTrace()
                emitAll(emptyFlow())
            }.collect { contents ->
                contents.forEach { it.let(relatedContents::add) }
            }
        }
    }

    fun fetchCachedEpisodes():
            Flow<List<EpisodeEntity>> = db?.episodeDao()?.all() ?: emptyFlow()

    fun fetchEpisodes(content: Content) {
        viewModelScope.launch(Dispatchers.IO) {
            val finishedDeferred = async {
                db?.contentDao()?.collectedContent(content.shikimoriID)?.let { collectedContent ->
                    repositories.forEach { source ->
                        source.searchEpisodes(content).collect { list ->
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

    fun switchSourcePinStatus(context: Context, id: Int, repository: AbstractContentRepository) {
        viewModelScope.launch(Dispatchers.IO) {
            val content = db?.contentDao()?.getContent(id)
            val subscriptionAllowed =
                AppDataStore.read(context, DataStoreScheme.FIELD_SUBSCRIPTION.key).firstOrNull()
                    ?: DataStoreScheme.FIELD_SUBSCRIPTION.defaultValue

            content?.let { entity ->
                val contentTopic = Util.encodeTopic(
                    repository = repository.name,
                    actingTeam = repository.name,
                    contentEnName = entity.enName
                )

                when (pinnedSources.contains(repository.name)) {
                    true -> {
                        pinnedSources.remove(repository.name)
                        if(subscriptionAllowed) Firebase.messaging.unsubscribeFromTopic(contentTopic)
                    }
                    else -> {
                        pinnedSources.add(repository.name)
                        if(subscriptionAllowed) Firebase.messaging.subscribeToTopic(contentTopic)
                    }
                }

                db?.contentDao()?.updateContents(entity.copy(pinnedSources = pinnedSources))
            }
        }
    }

    fun clearNotifications(shikimoriId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            db?.notificationDao()?.notificationsFromParent(shikimoriId)?.catch {
                it.printStackTrace()
                emitAll(emptyFlow())
            }?.map {
                it.toTypedArray()
            }?.collect {
                db.notificationDao().deleteNotification(*it)
            }
        }
    }
}