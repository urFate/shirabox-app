package org.shirabox.app.ui.activity.resource

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import org.shirabox.app.ValuesHelper
import org.shirabox.app.service.media.MediaDownloadsService
import org.shirabox.app.service.media.model.MediaDownloadTask
import org.shirabox.core.datastore.AppDataStore
import org.shirabox.core.datastore.DataStoreScheme
import org.shirabox.core.db.AppDatabase
import org.shirabox.core.entity.EpisodeEntity
import org.shirabox.core.model.ActingTeam
import org.shirabox.core.model.Content
import org.shirabox.core.model.ContentType.ANIME
import org.shirabox.core.model.Quality
import org.shirabox.core.model.ShiraBoxAnime
import org.shirabox.core.util.Util.Companion.mapContentToEntity
import org.shirabox.core.util.Util.Companion.mapEntityToContent
import org.shirabox.data.EpisodesHelper
import org.shirabox.data.catalog.shikimori.ShikimoriRepository
import org.shirabox.data.content.ContentRepositoryRegistry
import org.shirabox.data.shirabox.ShiraBoxRepository
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ResourceViewModel @Inject constructor(@ApplicationContext context: Context) : ViewModel() {
    private val db = AppDatabase.getAppDataBase(context)!!
    private val episodesHelper = EpisodesHelper(db)

    val content = mutableStateOf<Content?>(null)
    val relatedContents = mutableStateListOf<Content>()

    val internalContentUid = mutableLongStateOf(-1)
    val downloadGroupId = mutableIntStateOf(-1)

    val isFavourite = mutableStateOf(false)
    val episodesNotifications = mutableStateOf(false)
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

                    // Set data from ShiraBox API if possible
                    shiraBoxAnime.value?.let { anime ->
                        finalCache = cache.copy(shiraboxId = anime.id, image = anime.image)
                        database.contentDao().updateContents(finalCache)
                    }

                    content.value = mapEntityToContent(finalCache)
                    isFavourite.value = cache.isFavourite
                    episodesNotifications.value = cache.episodesNotifications
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

                        // Set shirabox API data
                        anime = shiraBoxAnime.value?.let { anime.copy(shiraboxId = it.id, image = it.image) } ?: anime

                        when(cachedData){
                            null -> {
                                val newUid = database.contentDao().insertContents(
                                    mapContentToEntity(
                                        content = anime,
                                        isFavourite = false,
                                        lastViewTimestamp = System.currentTimeMillis(),
                                        episodesNotifications = false,
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
                                        episodesNotifications = cachedData.episodesNotifications,
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
            ShikimoriRepository.fetchRelated(shikimoriID, ANIME)
                .catch {
                    it.printStackTrace()
                    emitAll(emptyFlow())
                }
                .collect { contents ->
                    relatedContents.clear()
                    contents.forEach { it.let(relatedContents::add) }
                }
        }
    }

    fun fetchCachedEpisodes():
            Flow<List<EpisodeEntity>> = db.episodeDao().all()

    fun fetchEpisodes(content: Content) {
        val finishedDeferred = viewModelScope.async(Dispatchers.IO) {
            val combinedContent = db.contentDao().getCombinedContent(content.shikimoriId)

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

    fun saveEpisodes(context: Context, quality: Quality, vararg episodes: EpisodeEntity) {
        Log.d("DOWNLOAD_D", "Saving episodes...")
        viewModelScope.launch(Dispatchers.IO) {
            val tasks = episodes
                .filter {
                    it.offlineVideos.isNullOrEmpty()
                }
                .map { entity ->
                    val uuid = UUID.randomUUID()
                    val repository = ContentRepositoryRegistry.getRepositoryByName(entity.source)!!

                    val destination = File(
                        context.filesDir,
                        ValuesHelper.buildOfflineMediaPath(
                            contentUid = entity.contentUid,
                            quality = quality,
                            fileName = uuid.toString()
                        )
                    )

                    val url = entity.videos[quality]
                        ?: entity.videos.toSortedMap(compareBy { it.quality }).values.last()

                    MediaDownloadTask(
                        url = url,
                        file = destination.path,
                        quality = quality,
                        streamProtocol = repository.streamingType,
                        group = entity.actingTeamName,
                        contentUid = entity.contentUid,
                        uid = entity.uid
                    )
            }

            context.startService(Intent(context, MediaDownloadsService::class.java))
            MediaDownloadsService.helper.enqueue(*tasks.toTypedArray())
        }
    }

    fun deleteOfflineEpisodes(vararg episodes: EpisodeEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            episodes.forEach { entity ->
                val offlinePaths = entity.offlineVideos

                offlinePaths?.values?.forEach {
                    val destination = File(it)
                    destination.delete()
                }

                db.episodeDao().updateEpisodes(entity.copy(offlineVideos = null))
            }
        }
    }


    fun refresh(content: Content) {
        viewModelScope.launch(Dispatchers.IO) {
            isRefreshing.value = true
            fetchContent(content.shikimoriId, true)
            fetchRelated(content.shikimoriId)
            fetchEpisodes(content)
            delay(2000L)
            isRefreshing.value = false
        }
    }

    fun switchFavouriteStatus(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            if(internalContentUid.longValue > -1) {
                isFavourite.value = isFavourite.value.not()

                var cachedContent = db.contentDao().getContentByUid(internalContentUid.longValue)

                cachedContent = cachedContent.copy(isFavourite = isFavourite.value)

                val subscriptionAllowed =
                    AppDataStore.read(context, DataStoreScheme.FIELD_SUBSCRIPTION.key).firstOrNull()
                        ?: DataStoreScheme.FIELD_SUBSCRIPTION.defaultValue

                shiraBoxAnime.value?.let {
                    cachedContent = cachedContent.copy(episodesNotifications = isFavourite.value && subscriptionAllowed)
                }

                db.contentDao().updateContents(cachedContent)
            }
        }
    }

    fun switchNotificationsStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            shiraBoxAnime.value?.let {
                val cachedContent = db.contentDao().getContentByUid(internalContentUid.longValue)

                episodesNotifications.value = cachedContent.episodesNotifications.not()
                db.contentDao().updateContents(
                    cachedContent.copy(episodesNotifications = cachedContent.episodesNotifications.not())
                )
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