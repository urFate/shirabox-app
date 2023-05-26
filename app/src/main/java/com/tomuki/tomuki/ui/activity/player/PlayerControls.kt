package com.tomuki.tomuki.ui.activity.player

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.tomuki.tomuki.R
import com.tomuki.tomuki.ui.theme.BrandRed
import com.tomuki.tomuki.util.Util
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ControlsScaffold(
    title: String, episode: Int, exoPlayer: ExoPlayer,
    orientationState: MutableState<Int>,
    controlsVisibilityState: MutableState<Boolean>,
    bottomSheetVisibilityState: MutableState<Boolean>
) {

    var isPlaying by remember {
        mutableStateOf(exoPlayer.isPlaying)
    }
    var currentPosition by remember {
        mutableStateOf(exoPlayer.currentPosition)
    }
    var totalDuration by remember {
        mutableStateOf(exoPlayer.duration)
    }
    var bufferedPercentage by remember {
        mutableStateOf(exoPlayer.bufferedPercentage)
    }
    var playbackState by remember {
        mutableStateOf(exoPlayer.playbackState)
    }
    var hasNextMediaItem by remember {
        mutableStateOf(exoPlayer.hasNextMediaItem())
    }
    var hasPreviousMediaItem by remember {
        mutableStateOf(exoPlayer.hasPreviousMediaItem())
    }

    val activity = LocalContext.current as Activity
    val coroutineScope = rememberCoroutineScope()

    activity.requestedOrientation = orientationState.value

    /**
     * FIXME: Any better solution to update timeline?
     */

    LaunchedEffect(key1 = true) {
        while (true) {
            isPlaying = exoPlayer.isPlaying
            totalDuration = exoPlayer.duration
            currentPosition = exoPlayer.contentPosition
            bufferedPercentage = exoPlayer.bufferedPercentage
            playbackState = exoPlayer.playbackState
            hasNextMediaItem = exoPlayer.hasNextMediaItem()
            hasPreviousMediaItem = exoPlayer.hasPreviousMediaItem()

            delay(400)
        }
    }

    Scaffold(
        topBar = {
            PlayerTopBar(title, episode) {
                bottomSheetVisibilityState.value = true
            }
        },
        bottomBar = {
            PlayerBottomBar(
                bufferedPercentage = bufferedPercentage,
                currentPosition = currentPosition,
                duration = totalDuration,
                orientationState = orientationState
            ) {
                exoPlayer.seekTo(it)
            }
        },
        content = {
            PlaybackControls(
                modifier = Modifier.padding(it),
                isPlaying = isPlaying,
                isLoaded = playbackState == Player.STATE_READY,
                hasNextMediaItem = hasNextMediaItem,
                hasPreviousMediaItem = hasPreviousMediaItem,
                onSkipPrevious = { exoPlayer.seekToPrevious() },
                onPlayToggle = {
                    exoPlayer.playWhenReady = !exoPlayer.isPlaying

                    coroutineScope.launch {
                        hideControls(exoPlayer, controlsVisibilityState)
                    }
                },
                onSkipNext = { exoPlayer.seekToNext() }
            )
        },
        containerColor = Color(0x80000000)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerTopBar(title: String, episode: Int, onSettingsClick: () -> Unit) {
    val activity = LocalContext.current as Activity

    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        title = {
            Column {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = title,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(id = R.string.episode_string, episode),
                    fontSize = 14.sp
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = { activity.finish() }) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = null
                )
            }
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0x00000000),
            navigationIconContentColor = MaterialTheme.colorScheme.inverseOnSurface,
            titleContentColor = MaterialTheme.colorScheme.inverseOnSurface,
            actionIconContentColor = MaterialTheme.colorScheme.inverseOnSurface,
        )
    )
}

@Composable
fun PlaybackControls(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    isLoaded: Boolean,
    hasNextMediaItem: Boolean,
    hasPreviousMediaItem: Boolean,
    onSkipPrevious: () -> Unit,
    onPlayToggle: () -> Unit,
    onSkipNext: () -> Unit
) {
    Box(
        modifier = modifier
            .then(
                Modifier.fillMaxSize()
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = !isLoaded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CircularProgressIndicator()
        }

        AnimatedVisibility(
            visible = isLoaded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(48.dp)
            ) {
                PlaybackIconButton(
                    imageVector = Icons.Outlined.SkipPrevious,
                    isActive = hasPreviousMediaItem,
                    onClick = onSkipPrevious
                )
                PlaybackIconButton(
                    imageVector = if (isPlaying)
                        Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                    onClick = onPlayToggle
                )
                PlaybackIconButton(
                    imageVector = Icons.Outlined.SkipNext,
                    isActive = hasNextMediaItem,
                    onClick = onSkipNext
                )
            }
        }
    }
}

@Composable
fun PlayerBottomBar(
    currentPosition: Long,
    duration: Long,
    bufferedPercentage: Int,
    orientationState: MutableState<Int>,
    onSliderValueChange: (Long) -> Unit,
) {
    Box(
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp, 16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "${Util.formatMilliseconds(currentPosition)} • ${
                    Util.formatMilliseconds(
                        duration
                    )
                }",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.inverseOnSurface
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Slider(
                    modifier = Modifier.weight(1f, false),
                    value = (bufferedPercentage / 100f),
                    onValueChange = { value ->
                        val newTime = (value * duration).toLong()
                        onSliderValueChange(newTime)
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = BrandRed,
                        activeTrackColor = BrandRed,
                        inactiveTrackColor = Color(0xCD252525)
                    )
                )

                IconButton(
                    onClick = {
                        orientationState.value =
                            if (orientationState.value == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
                                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            } else {
                                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                            }
                    }
                ) {
                    Icon(
                        imageVector = if (orientationState.value ==
                            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) Icons.Rounded.Fullscreen
                        else Icons.Rounded.FullscreenExit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.inverseOnSurface
                    )
                }
            }
        }
    }
}

@Composable
fun PlaybackIconButton(isActive: Boolean = true, imageVector: ImageVector, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        enabled = isActive
    ) {
        Icon(
            modifier = Modifier.size(42.dp),
            imageVector = imageVector,
            contentDescription = null,
            tint = if(isActive) MaterialTheme.colorScheme.inverseOnSurface
                else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
suspend fun hideControls(
    exoPlayer: ExoPlayer,
    state: MutableState<Boolean>
) {
    delay(3000).let {
        if (exoPlayer.isPlaying) state.value = false
    }
}