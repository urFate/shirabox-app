package live.shirabox.shirabox.ui.activity.player

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.launch
import live.shirabox.core.datastore.DataStoreScheme
import live.shirabox.core.entity.EpisodeEntity
import live.shirabox.core.model.Quality


@OptIn(UnstableApi::class)
@Composable
fun ShiraPlayer(exoPlayer: ExoPlayer, model: PlayerViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val interactionSource = remember(::MutableInteractionSource)

    val playlist by model.playlistFlow().collectAsStateWithLifecycle(initialValue = emptyList())

    Column {
        Box(
            modifier = Modifier
                .background(Color(0xFF000000))
                .fillMaxSize()
                .clickable(
                    interactionSource = interactionSource, indication = null
                ) {
                    coroutineScope.launch {
                        model.controlsVisibilityState = !model.controlsVisibilityState
                        hideControls(exoPlayer, model)
                    }
                }
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = playlist.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                PlayerSurface(
                    exoPlayer = exoPlayer,
                    model = model,
                    playlist = playlist
                )
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun PlayerSurface(exoPlayer: ExoPlayer, model: PlayerViewModel, playlist: List<EpisodeEntity>) {
    val context = LocalContext.current
    val playerView = PlayerView(context)
    val coroutineScope = rememberCoroutineScope()

    val defaultQualityState = model.defaultQualityPreferenceFlow(context)
        .collectAsState(initial = DataStoreScheme.FIELD_DEFAULT_QUALITY.defaultValue)
    val defaultQuality = remember(defaultQualityState) {
        defaultQualityState.value ?: DataStoreScheme.FIELD_DEFAULT_QUALITY.defaultValue
    }

    LaunchedEffect(Unit) {
        model.currentQuality = Quality.valueOfInt(defaultQuality)
        model.fetchEpisodePositions()
    }

    LaunchedEffect(model.episodesPositions[model.initialEpisode]) {
        val startPosition = model.episodesPositions[model.initialEpisode]

        if (!model.coldStartSeekApplied) startPosition?.let { startPos ->
            exoPlayer.apply {
                setMediaItems(playlist.map { episode ->
                    // Choose stream quality using default value, otherwise use highest available
                    val stream = episode.videos.entries.findLast {
                        it.key == Quality.valueOfInt(defaultQuality)
                    } ?: episode.videos.maxBy { it.key.quality }

                    model.currentQuality = stream.key

                    return@map MediaItem.fromUri(stream.value)
                })

                exoPlayer.seekTo(
                    playlist.indexOfFirst { it.episode == model.initialEpisode },
                    startPos
                )

                playWhenReady = true
            }

            model.coldStartSeekApplied = true
        }
    }

    LaunchedEffect(playlist) {
        if (playlist.isNotEmpty()) model.seekNewEpisodes(playlist)
    }

    LaunchedEffect(model.currentQuality, playlist.size) {
        if (exoPlayer.mediaItemCount != 0) {
            rebuildExoPlaylist(
                exoPlayer,
                playlist,
                model.currentQuality
            )
        }
    }

    LaunchedEffect(model.playbackSpeed) { exoPlayer.setPlaybackSpeed(model.playbackSpeed) }

    DisposableEffect(key1 = Unit) {
        exoPlayer.addListener(PlayerStateListener(coroutineScope, model))
        exoPlayer.pauseAtEndOfMediaItems = true

        onDispose {
            val currentEpisode = playlist.getOrNull(exoPlayer.currentMediaItemIndex)?.episode

            saveEpisodeState(currentEpisode, exoPlayer, model)
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
                keepScreenOn = true
                useController = false
            }
        }
    )

    ControlsScaffold(exoPlayer = exoPlayer, playlist = playlist, model = model)

    SettingsBottomSheet(exoPlayer = exoPlayer, playlist = playlist, model = model)
}

private fun rebuildExoPlaylist(
    exoPlayer: ExoPlayer,
    playlist: List<EpisodeEntity>,
    currentQuality: Quality
) {
    val currentPosition = exoPlayer.currentPosition
    val currentItemIndex = exoPlayer.currentMediaItemIndex

    exoPlayer.apply {
        setMediaItems(
            playlist.map { MediaItem.fromUri(it.videos[currentQuality] ?: "") },
            currentItemIndex,
            currentPosition
        )
    }
}

private fun saveEpisodeState(currentEpisode: Int?, exoPlayer: ExoPlayer, model: PlayerViewModel) {
    currentEpisode?.let {
        if (exoPlayer.playbackState == ExoPlayer.STATE_READY || exoPlayer.playbackState == ExoPlayer.STATE_ENDED) {
            model.saveEpisodePosition(currentEpisode, exoPlayer.currentPosition, exoPlayer.duration)
        }
    }
}