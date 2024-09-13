package org.shirabox.app.ui.activity.player

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.shirabox.core.datastore.AppDataStore
import org.shirabox.core.datastore.DataStoreScheme
import org.shirabox.core.db.AppDatabase
import org.shirabox.core.entity.EpisodeEntity
import org.shirabox.core.model.ContentType
import org.shirabox.core.model.Quality
import org.shirabox.core.util.Util
import org.shirabox.data.EpisodesHelper
import org.shirabox.data.animeskip.AnimeSkipRepository
import org.shirabox.data.content.ContentRepositoryRegistry

@HiltViewModel(assistedFactory = PlayerViewModel.PlayerViewModelFactory::class)
class PlayerViewModel @AssistedInject constructor(
    @Assisted val contentUid: Long,
    @Assisted("contentName") val contentName: String,
    @Assisted("contentEnName") val contentEnName: String,
    @Assisted("team") val team: String,
    @Assisted("repository") val repository: String,
    @Assisted val initialEpisode: Int,
    @ApplicationContext context: Context
) : ViewModel() {

    private val db = AppDatabase.getAppDataBase(context)!!
    private val episodesHelper = EpisodesHelper(db)
    private val repositories =
        ContentRepositoryRegistry.REPOSITORIES.filter { it.contentType == ContentType.ANIME }

    val episodesPositions = mutableStateMapOf<Int, Long>()
    var controlsVisibilityState by mutableStateOf(true)
    var bottomSheetVisibilityState by mutableStateOf(false)
    var currentQuality by mutableStateOf(Quality.HD)
    var playbackSpeed by mutableFloatStateOf(1F)
    var coldStartSeekApplied by mutableStateOf(false)
    val animeSkipTimestamps = MutableStateFlow<Map<Int, Pair<Long, Long>>>(emptyMap())

    @AssistedFactory
    interface PlayerViewModelFactory {
        fun create(
            contentUid: Long,
            @Assisted("contentName") contentName: String,
            @Assisted("contentEnName") contentEnName: String,
            @Assisted("team") team: String,
            @Assisted("repository") repository: String,
            initialEpisode: Int
        ): PlayerViewModel
    }

    fun playlistFlow() = db.episodeDao().getEpisodes(contentUid, team, repository).map { list ->
        list.sortedBy { it.episode }
    }

    fun seekNewEpisodes(cachedEpisodes: List<EpisodeEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            val content = db.contentDao().getContentByUid(contentUid)
            repositories.forEach {
                episodesHelper.partialEpisodesSearch(
                    repository = it,
                    content = Util.mapEntityToContent(content),
                    contentUid = content.uid,
                    cachedEpisodes = cachedEpisodes,
                    range = cachedEpisodes.last().episode.inc()..Int.MAX_VALUE
                )
            }
        }
    }

    fun saveEpisodePosition(episode: Int, time: Long, length: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val episodeEntity =
                db.episodeDao().getEpisode(contentUid, episode, team, repository)
            val contentEntity = db.contentDao().getContentByUid(contentUid)

            episodeEntity.let {
                db.episodeDao().updateEpisodes(
                    it.copy(
                        watchingTime = time,
                        videoLength = length,
                        viewTimestamp = System.currentTimeMillis()
                    )
                )
            }

            contentEntity.let {
                db.contentDao()
                    .updateContents(contentEntity.copy(lastViewTimestamp = System.currentTimeMillis()))
            }
        }
    }

    fun fetchEpisodePositions() {
        viewModelScope.launch(Dispatchers.IO) {
            db.episodeDao().getEpisodes(contentUid, team, repository).collect { entityList ->
                episodesPositions.putAll(entityList.associate {
                    it.episode to it.watchingTime
                })
            }
        }
    }

    fun fetchAnimeSkipIntroTimestamps(context: Context, episode: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val clientId = AppDataStore.read(context, DataStoreScheme.FIELD_ANIMESKIP_USER_CLIENT_ID)
                .firstOrNull() ?: return@launch

            val showId = AnimeSkipRepository.searchShowId(contentEnName, clientId)
                .catch {
                    it.printStackTrace()
                }.firstOrNull()

            val timestamps = showId?.let {
                AnimeSkipRepository.findEpisodeIntroTimestamps(
                    showId = showId,
                    episode = episode,
                    clientKey = clientId
                ).catch {
                    it.printStackTrace()
                }.firstOrNull()
            }




            timestamps?.let {
                val currentValue = animeSkipTimestamps.value.toMutableMap()
                currentValue[episode] = timestamps.first.toLong() to timestamps.second.toLong()

                animeSkipTimestamps.emit(currentValue)
            }
        }
    }

    fun defaultQualityPreferenceFlow(context: Context): Flow<Int?> =
        AppDataStore.read(context, DataStoreScheme.FIELD_DEFAULT_QUALITY.key)

    fun openingSkipPreferenceFlow(context: Context): Flow<Boolean?> =
        AppDataStore.read(context, DataStoreScheme.FIELD_OPENING_SKIP.key)

    fun instantSeekPreferenceFlow(context: Context): Flow<Int?> =
        AppDataStore.read(context, DataStoreScheme.FIELD_INSTANT_SEEK_TIME.key)
}