package org.shirabox.app.ui.activity.player.presentation

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.shirabox.app.ui.activity.player.PlayerViewModel
import org.shirabox.app.ui.activity.player.presentation.controls.InstantSeekArea
import org.shirabox.app.ui.activity.player.presentation.controls.PlaybackControls
import org.shirabox.app.ui.activity.player.presentation.controls.PlayerBottomBar
import org.shirabox.app.ui.activity.player.presentation.controls.PlayerSkipButton
import org.shirabox.app.ui.activity.player.presentation.controls.PlayerTopBar
import org.shirabox.core.datastore.DataStoreScheme
import org.shirabox.core.entity.EpisodeEntity
import org.shirabox.core.util.Values

@Composable
fun PlayerScaffold(exoPlayer: ExoPlayer, playlist: List<EpisodeEntity>, model: PlayerViewModel) {
    val context = LocalContext.current
    val activity = context as Activity
    val coroutineScope = rememberCoroutineScope()

    var isPlaying by remember { mutableStateOf(exoPlayer.isPlaying) }
    var currentPosition by remember { mutableLongStateOf(exoPlayer.currentPosition) }
    var totalDuration by remember { mutableLongStateOf(exoPlayer.duration) }
    var playbackState by remember { mutableIntStateOf(exoPlayer.playbackState) }
    var hasNextMediaItem by remember { mutableStateOf(exoPlayer.hasNextMediaItem()) }
    var hasPreviousMediaItem by remember { mutableStateOf(exoPlayer.hasPreviousMediaItem()) }
    var currentMediaItemIndex by remember { mutableIntStateOf(exoPlayer.currentMediaItemIndex) }

    val animeSkipTimestamps by model.animeSkipTimestamps.collectAsStateWithLifecycle()

    val currentEpisode = remember(currentMediaItemIndex) {
        playlist.getOrNull(exoPlayer.currentMediaItemIndex)?.episode ?: 1
    }

    val providedIntroMarkers = remember(currentMediaItemIndex) {
        playlist[exoPlayer.currentMediaItemIndex].videoMarkers
    }
    val animeSkipIntroMarkers = remember(currentMediaItemIndex) {
        animeSkipTimestamps[currentEpisode]
    }

    // Preferences dependent values

    val openingSkipPreferenceFlow =
        model.openingSkipPreferenceFlow(LocalContext.current).collectAsState(
            initial = DataStoreScheme.FIELD_OPENING_SKIP.defaultValue
        )
    val openingAutoSkip = remember(openingSkipPreferenceFlow.value) {
        openingSkipPreferenceFlow.value ?: DataStoreScheme.FIELD_OPENING_SKIP.defaultValue
    }

    val instantSeekPreferenceFlow = model.instantSeekPreferenceFlow(LocalContext.current).collectAsState(
        initial = DataStoreScheme.FIELD_INSTANT_SEEK_TIME.defaultValue
    )
    val instantSeekTime = remember(instantSeekPreferenceFlow.value) {
        derivedStateOf {
            (instantSeekPreferenceFlow.value ?: DataStoreScheme.FIELD_INSTANT_SEEK_TIME.defaultValue)
        }
    }

    val forceHideSkipButton = remember { mutableStateOf(false) }

    val showSkipButton = remember(currentPosition, forceHideSkipButton) {
        if (forceHideSkipButton.value || playbackState == Player.STATE_BUFFERING) {
            return@remember false
        }

        animeSkipIntroMarkers?.let {
            return@remember currentPosition in it.first..it.second
        }

        providedIntroMarkers.let {
            return@remember currentPosition in it.first..it.second
        }
    }

    LaunchedEffect(currentMediaItemIndex) {
        model.fetchAnimeSkipIntroTimestamps(context = context, episode = currentEpisode)
    }

    /**
     * FIXME: Any better solution to update timeline?
     */

    LaunchedEffect(true) {
        while (true) {
            isPlaying = exoPlayer.isPlaying
            totalDuration = exoPlayer.duration
            currentPosition = exoPlayer.contentPosition
            playbackState = exoPlayer.playbackState
            hasNextMediaItem = exoPlayer.hasNextMediaItem()
            hasPreviousMediaItem = exoPlayer.hasPreviousMediaItem()
            currentMediaItemIndex = exoPlayer.currentMediaItemIndex.inc()

            delay(200)
        }
    }

    InstantSeekArea(
        seekOffset = instantSeekTime.value,
        onFastRewind = { multiplier ->
            exoPlayer.seekTo(
                (exoPlayer.currentPosition - (instantSeekTime.value.times(1000L) * multiplier))
                    .coerceAtLeast(0)
            )
        },
        onFastForward = { multiplier ->
            exoPlayer.seekTo(
                (exoPlayer.currentPosition + (instantSeekTime.value.times(1000L) * multiplier))
                    .coerceAtMost(exoPlayer.duration)
            )
        },
        onClick = {
            coroutineScope.launch {
                model.controlsVisibilityState = !model.controlsVisibilityState
                hideControls(exoPlayer, model)
            }
        }
    )

    AnimatedVisibility(
        visible = model.controlsVisibilityState,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Scaffold(
            topBar = {
                PlayerTopBar(
                    title = model.contentName,
                    episode = currentEpisode,
                    onBackClick = { activity.finish() },
                ) {
                    model.bottomSheetVisibilityState = true
                }
            },
            bottomBar = {
                PlayerBottomBar(
                    currentPosition = currentPosition,
                    duration = totalDuration,
                    model = model,
                    onSliderValueChange = {
                        model.controlsVisibilityState = true
                    }
                ) {
                    exoPlayer.seekTo(it)
                }
            },
            content = {
                val isLoaded = (playbackState == Player.STATE_READY) || (playbackState == Player.STATE_ENDED)

                PlaybackControls(
                    modifier = Modifier.padding(it),
                    isPlaying = isPlaying,
                    isLoaded = isLoaded,
                    hasNextMediaItem = hasNextMediaItem,
                    hasPreviousMediaItem = hasPreviousMediaItem,
                    onSkipPrevious = {
                        model.saveEpisodePosition(currentEpisode, exoPlayer.currentPosition, exoPlayer.duration)
                        exoPlayer.seekToPrevious()
                    },
                    onPlayToggle = {
                        exoPlayer.playWhenReady = !exoPlayer.isPlaying
                        coroutineScope.launch { hideControls(exoPlayer, model) }
                    },
                    onSkipNext = {
                        model.saveEpisodePosition(currentEpisode, exoPlayer.currentPosition, exoPlayer.duration)
                        exoPlayer.seekToNext()
                    }
                )
            },
            containerColor = Color(0x80000000)
        )
    }

    AnimatedVisibility(
        visible = showSkipButton,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        val introEnd = animeSkipIntroMarkers?.second ?: providedIntroMarkers.second

        PlayerSkipButton(
            autoSkip = openingAutoSkip,
            isPlaying = isPlaying,
            onTimeout = {
                introEnd.let(exoPlayer::seekTo)
                model.controlsVisibilityState = true
                forceHideSkipButton.value = true
            },
            onClick = {
                if (openingAutoSkip) {
                    forceHideSkipButton.value = true
                } else {
                    introEnd.let(exoPlayer::seekTo)
                    model.controlsVisibilityState = true
                }
        })
    }
}

suspend fun hideControls(
    exoPlayer: ExoPlayer,
    model: PlayerViewModel
) {
    val delayMs = Values.CONTROLS_HIDE_DELAY

    delay(delayMs).let {
        if (exoPlayer.isPlaying) model.controlsVisibilityState = false
    }
}