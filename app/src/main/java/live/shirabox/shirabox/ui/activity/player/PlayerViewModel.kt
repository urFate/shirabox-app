package live.shirabox.shirabox.ui.activity.player

import android.content.Context
import android.content.pm.ActivityInfo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import live.shirabox.core.datastore.AppDataStore
import live.shirabox.core.datastore.DataStoreScheme
import live.shirabox.core.model.ActingTeam
import live.shirabox.core.model.PlaylistVideo
import live.shirabox.data.animeskip.AnimeSkipRepository
import live.shirabox.shirabox.db.AppDatabase

class PlayerViewModel(
    context: Context,
    val contentUid: Long,
    val contentName: String,
    val contentEnName: String,
    val actingTeam: ActingTeam,
    val episode: Int,
    val startIndex: Int,
    val playlist: List<PlaylistVideo>
) : ViewModel() {
    private val db = AppDatabase.getAppDataBase(context)

    val episodesPositions = mutableStateMapOf<Int, Long>()
    var controlsVisibilityState by mutableStateOf(true)
    var bottomSheetVisibilityState by mutableStateOf(false)
    var orientationState by mutableIntStateOf(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
    var currentQuality by mutableStateOf(live.shirabox.core.model.Quality.HD)
    var playbackSpeed by mutableFloatStateOf(1F)
    val animeSkipTimestamps = mutableStateMapOf<Int, Pair<Long, Long>>()

    fun saveEpisodePosition(episode: Int, time: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val episodeEntity =
                db?.episodeDao()?.getEpisode(contentUid, episode, actingTeam)

            episodeEntity?.let { db?.episodeDao()?.updateEpisodes(it.copy(watchingTime = time)) }
        }
    }

    fun fetchEpisodePositions() {
        viewModelScope.launch(Dispatchers.IO) {
            db?.episodeDao()?.getEpisodes(contentUid, actingTeam)?.collect { entityList ->
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
                animeSkipTimestamps[episode] = timestamps.first.toLong() to timestamps.second.toLong()
            }
        }
    }

    fun defaultQualityPreferenceFlow(context: Context): Flow<Int?> {
        return AppDataStore.read(context, DataStoreScheme.FIELD_DEFAULT_QUALITY.key)
    }
    fun openingSkipPreferenceFlow(context: Context): Flow<Boolean?> {
        return AppDataStore.read(context, DataStoreScheme.FIELD_OPENING_SKIP.key)
    }
}