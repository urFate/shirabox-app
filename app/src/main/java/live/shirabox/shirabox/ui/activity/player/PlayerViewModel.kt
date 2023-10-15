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
import kotlinx.coroutines.launch
import live.shirabox.core.model.PlaylistVideo
import live.shirabox.shirabox.db.AppDatabase

class PlayerViewModel(
    context: Context,
    val contentUid: Int,
    val contentName: String,
    val startEpisode: Int,
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
                    it.episode.dec() to it.watchingTime
                })
            }
        }
    }
}