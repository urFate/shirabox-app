package com.tomuki.tomuki.ui.activity.player

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.tomuki.tomuki.ui.theme.TomukiTheme
import com.tomuki.tomuki.util.Util
import kotlinx.coroutines.launch

class PlayerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TomukiTheme(
                darkTheme = false
            ) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // TODO: Test stream
                    val stream = remember {
                        "https://cache.libria.fun/videos/media/ts/9000/1/480" +
                                "/8a7f4d218433f5a5fee1c6f5a02d278e.m3u8"
                    }

                    val systemUiController = rememberSystemUiController()

                    systemUiController.setStatusBarColor(
                        color = Color.Transparent,
                        darkIcons = false
                    )

                    Util.hideSystemUi(systemUiController)

                    VideoPlayer(stream)
                }
            }
        }
    }
}

@Composable
fun VideoPlayer(stream: String) {
    val context = LocalContext.current
    val playerView = PlayerView(context)

    val controlsVisibilityState = remember {
        mutableStateOf(true)
    }
    val bottomSheetVisibilityState = remember {
        mutableStateOf(false)
    }
    val orientationState = remember {
        mutableStateOf(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
    }
    val coroutineScope = rememberCoroutineScope()

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(
                MediaItem.fromUri(stream)
            )
            prepare()
            playWhenReady = true
        }
    }

    Column {
        Box(
            modifier = Modifier
                .background(Color(0xFF000000))
                .clickable(
                    interactionSource = MutableInteractionSource(), indication = null
                ) {
                    coroutineScope.launch {
                        controlsVisibilityState.value = !controlsVisibilityState.value
                        hideControls(exoPlayer, controlsVisibilityState)
                    }
                }
        ) {
            DisposableEffect(key1 = Unit) {
                exoPlayer.addListener(
                    PlayerLoadingStateListener(coroutineScope, exoPlayer, controlsVisibilityState)
                )
                onDispose { exoPlayer.release() }
            }

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

            this@Column.AnimatedVisibility(
                visible = controlsVisibilityState.value,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ControlsScaffold(
                    title = "Название",
                    episode = 1,
                    exoPlayer = exoPlayer,
                    orientationState = orientationState,
                    controlsVisibilityState = controlsVisibilityState,
                    bottomSheetVisibilityState = bottomSheetVisibilityState
                )
            }
        }

        SettingsBottomSheet(
            visibilityState = bottomSheetVisibilityState
        )
    }
}