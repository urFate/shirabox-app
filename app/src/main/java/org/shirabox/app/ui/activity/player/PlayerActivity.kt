package org.shirabox.app.ui.activity.player

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import org.shirabox.app.ui.theme.ShiraBoxTheme
import org.shirabox.core.util.Util

@AndroidEntryPoint
class PlayerActivity : ComponentActivity() {

    private var player: ExoPlayer? = null

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ShiraBoxTheme(
                transparentStatusBar = true,
                darkTheme = false
            ) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PlayerActivitySurface(intent = intent)
                }
            }
        }

        enableEdgeToEdge()
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    @OptIn(UnstableApi::class)
    @Composable
    private fun PlayerActivitySurface(intent: Intent) {
        val arguments = intent.extras
        val context = LocalContext.current

        rememberSystemUiController().apply { Util.hideSystemUi(this) }

        val model = hiltViewModel<PlayerViewModel, PlayerViewModel.PlayerViewModelFactory> {
            it.create(
                contentUid = arguments!!.getLong("content_uid"),
                contentName = arguments.getString("name").toString(),
                contentEnName = arguments.getString("en_name").toString(),
                team = arguments.getString("acting_team").toString(),
                repository = arguments.getString("repository").toString(),
                initialEpisode = arguments.getInt("episode"),
            )
        }

        val exoPlayer = remember { ExoPlayer.Builder(context).build().apply(ExoPlayer::prepare) }

        player = exoPlayer

        ShiraPlayer(exoPlayer = exoPlayer, model = model)
    }
}