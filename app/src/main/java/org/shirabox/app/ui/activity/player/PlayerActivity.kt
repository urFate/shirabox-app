package org.shirabox.app.ui.activity.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import org.shirabox.app.ui.theme.ShiraBoxTheme
import org.shirabox.core.util.Util
import org.shirabox.core.util.Values

@AndroidEntryPoint
class PlayerActivity : ComponentActivity() {

    private var player: ExoPlayer? = null

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ShiraBoxTheme(
                darkTheme = false
            ) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val arguments = intent.extras
                    val context = LocalContext.current

                    rememberSystemUiController().apply {
                        setStatusBarColor(
                            color = Color.Transparent,
                            darkIcons = false
                        )
                        Util.hideSystemUi(this)
                    }

                    val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                        .setAllowCrossProtocolRedirects(true).setUserAgent(Values.USER_AGENT)

                    val dataSourceFactory = DataSource.Factory { httpDataSourceFactory.createDataSource() }

                    val exoPlayer = remember {
                        ExoPlayer.Builder(context).setMediaSourceFactory(
                            DefaultMediaSourceFactory(context).setDataSourceFactory(
                                dataSourceFactory
                            )
                        ).build().apply { prepare() }
                    }
                    player = exoPlayer

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

                    ShiraPlayer(exoPlayer = exoPlayer, model = model)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }
    override fun onStop() {
        super.onStop()
        player?.pause()
    }
}