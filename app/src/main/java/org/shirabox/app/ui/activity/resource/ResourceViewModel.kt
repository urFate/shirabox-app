package org.shirabox.app.ui.activity.resource

import android.content.Context
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.shirabox.core.datastore.AppDataStore
import org.shirabox.core.datastore.DataStoreScheme
import org.shirabox.core.db.AppDatabase
import org.shirabox.core.entity.EpisodeEntity
import org.shirabox.core.model.ActingTeam
import org.shirabox.core.model.Content
import org.shirabox.core.model.ContentType.ANIME
import org.shirabox.core.model.ShiraBoxAnime
import org.shirabox.core.util.Util.Companion.mapContentToEntity
import org.shirabox.core.util.Util.Companion.mapEntityToContent
import org.shirabox.data.EpisodesHelper
import org.shirabox.data.catalog.shikimori.ShikimoriRepository
import org.shirabox.data.content.ContentRepositoryRegistry
import org.shirabox.data.shirabox.ShiraBoxRepository
import javax.inject.Inject

@HiltViewModel
class ResourceViewModel @Inject constructor(@ApplicationContext context: Context) : ViewModel() {
    private val db = AppDatabase.getAppDataBase(context)!!
    private val episodesHelper = EpisodesHelper(db)

    val content = mutableStateOf<Content?>(null)
    val relatedContents = mutableStateListOf<Content>()

    val internalContentUid = mutableLongStateOf(-1)

    val isFavourite = mutableStateOf(false)
    val pinnedTeams = mutableStateListOf<String>()

    val shiraBoxAnime = mutableStateOf<ShiraBoxAnime?>(null)

    val episodeFetchComplete = mutableStateOf(false)
    val isRefreshing = mutableStateOf(false)
    val contentObservationException = mutableStateOf<Exception?>(null)

    val repositories =
        ContentRepositoryRegistry.REPOSITORIES.filter { it.contentType == ANIME }

    fun fetchContent(shikimoriId: Int, forceRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            db.let { database ->
                val cachedData = database.contentDao().getContent(shikimoriId)

                // Fetch anime from ShiraBox API
                ShiraBoxRepository.fetchAnime(shikimoriId)
                    .catch { it.printStackTrace() }
                    .collectLatest { anime -> shiraBoxAnime.value = anime }

                // Use cached data if available
                cachedData?.let { cache ->
                    var finalCache = cache

                    // Set poster image from ShiraBox API if possible
                    shiraBoxAnime.value?.let {
                        if(cachedData.image != it.image) {
                            finalCache = cache.copy(image = it.image)
                            database.contentDao().updateContents(finalCache)
                        }
                    }

                    content.value = mapEntityToContent(finalCache)
                    isFavourite.value = cache.isFavourite
                    internalContentUid.longValue = cache.uid
                    pinnedTeams.addAll(cache.pinnedTeams)

                    if(!forceRefresh) return@launch
                }

                // Fetch data from APIs if content is not cached
                ShikimoriRepository.fetchContent(shikimoriId, ANIME)
                    .catch {
                        contentObservationException.value = it as Exception
                        it.printStackTrace()
                        emitAll(emptyFlow())
                    }
                    .collect { shikimoriContent ->
                        var anime = shikimoriContent

                        // Replace poster
                        anime = shiraBoxAnime.value?.let { anime.copy(image = it.image) } ?: anime

                        when(cachedData){
                            null -> {
                                val newUid = database.contentDao().insertContents(
                                    mapContentToEntity(
                                        content = anime,
                                        isFavourite = false,
                                        lastViewTimestamp = System.currentTimeMillis(),
                                        pinnedTeams = emptyList()
                                    )
                                ).first()

                                internalContentUid.longValue = newUid
                                content.value = anime
                            }

                            else -> {
                                database.contentDao().updateContents(
                                    mapContentToEntity(
                                        contentUid = cachedData.uid,
                                        content = anime,
                                        isFavourite = cachedData.isFavourite,
                                        lastViewTimestamp = cachedData.lastViewTimestamp,
                                        pinnedTeams = cachedData.pinnedTeams
                                    )
                                )
                            }
                        }
                }
            }
        }
    }

    fun fetchRelated(shikimoriID: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            ShikimoriRepository.fetchRelated(shikimoriID, ANIME).catch {
                it.printStackTrace()
                emitAll(emptyFlow())
            }.collect { contents ->
                contents.forEach { it.let(relatedContents::add) }
            }
        }
    }

    fun fetchCachedEpisodes():
            Flow<List<EpisodeEntity>> = db.episodeDao().all()

    fun fetchEpisodes(content: Content) {
        val finishedDeferred = viewModelScope.async(Dispatchers.IO) {
            val combinedContent = db.contentDao().getCombinedContent(content.shikimoriID)

            combinedContent.let {
                repositories.forEach { repository ->
                    val cachedEpisodes = combinedContent.episodes
                    val completeSearchRequired = cachedEpisodes.none { it.source == repository.name }

                    async {
                        when (completeSearchRequired) {
                            true -> episodesHelper.completeEpisodesSearch(
                                repository = repository,
                                content = content,
                                contentUid = combinedContent.content.uid,
                                cachedEpisodes = cachedEpisodes
                            )

                            false -> episodesHelper.partialEpisodesSearch(
                                repository = repository,
                                content = content,
                                contentUid = combinedContent.content.uid,
                                cachedEpisodes = cachedEpisodes,
                                range = cachedEpisodes.last().episode.dec()..Int.MAX_VALUE
                            )
                        }
                    }.await()
                }
            }
            true
        }

        viewModelScope.launch(Dispatchers.IO) {
            episodeFetchComplete.value = finishedDeferred.await()
        }
    }

    fun refresh(content: Content) {
        viewModelScope.launch(Dispatchers.IO) {
            isRefreshing.value = true
            fetchContent(content.shikimoriID, true)
            fetchRelated(content.shikimoriID)
            fetchEpisodes(content)
            delay(2000L)
            isRefreshing.value = false
        }
    }

    fun switchFavouriteStatus(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            isFavourite.value = !isFavourite.value

            launch {
                shiraBoxAnime.value?.let { anime ->
                    val subscriptionAllowed =
                        AppDataStore.read(context, DataStoreScheme.FIELD_SUBSCRIPTION.key).firstOrNull()
                            ?: DataStoreScheme.FIELD_SUBSCRIPTION.defaultValue

                    val topic = "id-${anime.id}"

                    if (subscriptionAllowed && isFavourite.value) {
                        Firebase.messaging.subscribeToTopic(topic)
                    } else {
                        Firebase.messaging.unsubscribeFromTopic(topic)
                    }
                }
            }

            launch {
                if(internalContentUid.longValue > -1) {
                    val content = db.contentDao().getContentByUid(internalContentUid.value)
                    db.contentDao().updateContents(content.copy(isFavourite = isFavourite.value))
                }
            }
        }
    }

    fun switchTeamPinStatus(
        id: Int,
        team: ActingTeam
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val content = db.contentDao().getContent(id)

            content?.let { entity ->
                if (pinnedTeams.contains(team.name)) {
                    pinnedTeams.remove(team.name)
                } else {
                    pinnedTeams.add(team.name)
                }

                db.contentDao().updateContents(entity.copy(pinnedTeams = pinnedTeams))
            }
        }
    }

    fun clearNotifications(shikimoriId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            db.notificationDao().notificationsFromParent(shikimoriId).catch {
                it.printStackTrace()
                emitAll(emptyFlow())
            }.map {
                it.toTypedArray()
            }.collect {
                db.notificationDao().deleteNotification(*it)
            }
        }
    }
}