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
import kotlinx.coroutines.launch
import live.shirabox.core.datastore.AppDataStore
import live.shirabox.core.datastore.DataStoreScheme
import live.shirabox.core.model.PlaylistVideo
import live.shirabox.shirabox.db.AppDatabase

class PlayerViewModel(
    context: Context,
    val contentUid: Long,
    val contentName: String,
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

    fun saveEpisodePosition(episode: Int, time: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val episodeEntity =
                db?.episodeDao()?.getEpisodeByParentAndEpisode(contentUid, episode)

            episodeEntity?.let { db?.episodeDao()?.updateEpisodes(it.copy(watchingTime = time)) }
        }
    }

    fun fetchEpisodePositions() {
        viewModelScope.launch(Dispatchers.IO) {
            db?.episodeDao()?.getEpisodesByParent(contentUid)?.collect { entityList ->
                episodesPositions.putAll(entityList.associate {
                    it.episode to it.watchingTime
                })
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