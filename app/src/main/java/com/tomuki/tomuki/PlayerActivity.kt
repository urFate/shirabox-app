package com.tomuki.tomuki

import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.tomuki.tomuki.ui.theme.TomukiTheme

class PlayerActivity : ComponentActivity() {

    @UnstableApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TomukiTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // TODO: Test stream
                    val stream = "https://cache.libria.fun/videos/media/ts/9000/1/480" +
                            "/8a7f4d218433f5a5fee1c6f5a02d278e.m3u8"

                    VideoPlayer(stream)
                }
            }
        }
    }
}

@UnstableApi
@Composable
fun VideoPlayer(stream: String){
    val context = LocalContext.current
    val playerView = PlayerView(context)

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(
                MediaItem.fromUri(stream)
            )
            prepare()
            playWhenReady = true
        }
    }

    Box {
        DisposableEffect(key1 = Unit) { onDispose { exoPlayer.release() } }

        AndroidView(
            factory = {
                playerView.apply {
                    player = exoPlayer
                    layoutParams =
                        FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    useController = false
                }
            }
        )
    }
}