package live.shirabox.shirabox.ui.activity.player

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.launch


@Composable
fun ShiraPlayer(model: PlayerViewModel) {
    val context = LocalContext.current
    val playerView = PlayerView(context)

    val coroutineScope = rememberCoroutineScope()

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply { prepare() }
    }

    val startPosition by remember {
        derivedStateOf { model.episodesPositions[model.startEpisode] }
    }

    LaunchedEffect(Unit) {
        model.fetchEpisodePositions()
    }

    LaunchedEffect(startPosition) {
        startPosition?.let {
            exoPlayer.apply {
                setMediaItems(model.playlist.map {
                    MediaItem.fromUri(
                        it.streamUrls[model.currentQuality] ?: ""
                    )
                })
                seekTo(model.startEpisode, it)
                playWhenReady = true
            }
        }
    }

    LaunchedEffect(model.currentQuality) {
        if (exoPlayer.mediaItemCount != 0) {
            val currentPosition = exoPlayer.currentPosition
            val currentItemIndex = exoPlayer.currentMediaItemIndex

            exoPlayer.apply {
                replaceMediaItems(
                    0,
                    model.playlist.size.dec(),
                    model.playlist.map {
                        MediaItem.fromUri(
                            it.streamUrls[model.currentQuality] ?: ""
                        )
                    })
                seekTo(currentItemIndex, currentPosition)
            }
        }
    }

    LaunchedEffect(model.playbackSpeed) { exoPlayer.setPlaybackSpeed(model.playbackSpeed) }

    Column {
        Box(
            modifier = Modifier
                .background(Color(0xFF000000))
                .clickable(
                    interactionSource = MutableInteractionSource(), indication = null
                ) {
                    coroutineScope.launch {
                        model.controlsVisibilityState = !model.controlsVisibilityState
                        hideControls(exoPlayer, model)
                    }
                }
        ) {
            DisposableEffect(key1 = Unit) {
                exoPlayer.addListener(
                    PlayerLoadingStateListener(coroutineScope, model)
                )
                onDispose {

                    // Save watching progress
                    model.saveEpisodePosition(
                        exoPlayer.currentMediaItemIndex.inc(),
                        exoPlayer.currentPosition
                    )

                    exoPlayer.release()
                }
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

            ControlsScaffold(exoPlayer = exoPlayer, model = model)
        }

        SettingsBottomSheet(exoPlayer = exoPlayer, model = model)
    }
}