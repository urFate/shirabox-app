package live.shirabox.shirabox.ui.activity.player

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import live.shirabox.core.util.Util
import live.shirabox.core.util.Values
import live.shirabox.shirabox.R

@Composable
fun ControlsScaffold(exoPlayer: ExoPlayer, model: PlayerViewModel) {

    var isPlaying by remember { mutableStateOf(exoPlayer.isPlaying) }
    var currentPosition by remember { mutableLongStateOf(exoPlayer.currentPosition) }
    var totalDuration by remember { mutableLongStateOf(exoPlayer.duration) }
    var playbackState by remember { mutableIntStateOf(exoPlayer.playbackState) }
    var hasNextMediaItem by remember { mutableStateOf(exoPlayer.hasNextMediaItem()) }
    var hasPreviousMediaItem by remember { mutableStateOf(exoPlayer.hasPreviousMediaItem()) }
    var currentMediaItemIndex by remember { mutableIntStateOf(exoPlayer.currentMediaItemIndex + 1) }

    val currentItemMarkers = remember(currentMediaItemIndex) {
        model.playlist[exoPlayer.currentMediaItemIndex].openingMarkers
    }

    val currentEpisode = remember(currentMediaItemIndex) {
        model.playlist[exoPlayer.currentMediaItemIndex].episode
    }

    val showSkipButton = remember(currentPosition) {
        currentItemMarkers.let {
            currentPosition in it.first..it.second
        }
    }

    val activity = LocalContext.current as Activity
    val coroutineScope = rememberCoroutineScope()

    activity.requestedOrientation = model.orientationState

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
            currentMediaItemIndex = exoPlayer.currentMediaItemIndex + 1

            delay(400)
        }
    }

    InstantSeekZones(
        onFastRewind = {
            exoPlayer.seekTo(exoPlayer.currentPosition.minus(Values.INSTANT_SEEK_TIME))
        },
        onFastForward = {
            exoPlayer.seekTo(exoPlayer.currentPosition.plus(Values.INSTANT_SEEK_TIME))
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
                PlayerTopBar(model.contentName, currentEpisode) {
                    model.bottomSheetVisibilityState = true
                }
            },
            bottomBar = {
                PlayerBottomBar(
                    currentPosition = currentPosition,
                    duration = totalDuration,
                    model = model
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
                        coroutineScope.launch { hideControls(exoPlayer, model) }
                    },
                    onSkipNext = { exoPlayer.seekToNext() }
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
        SkipButton {
            currentItemMarkers.let {
                exoPlayer.seekTo(it.second)
                model.controlsVisibilityState = true
            }
        }
    }
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
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
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
fun SkipButton(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp, 64.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        OutlinedButton(
            border = BorderStroke(1.dp, Color.White),
            onClick = onClick
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardDoubleArrowRight,
                    tint = Color.White,
                    contentDescription = "opening skip"
                )
                Text(text = stringResource(id = R.string.opening_skip), color = Color.White)
            }
        }
    }
}

@Composable
fun InstantSeekZones(
    onFastRewind: (offset: Offset) -> Unit,
    onFastForward: (offset: Offset) -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val deviceWidth = LocalConfiguration.current.screenWidthDp.dp

        var showRewindUi by remember {
            mutableStateOf(false)
        }
        var showForwardUi by remember {
            mutableStateOf(false)
        }

        val coroutineScope = rememberCoroutineScope()

        // Rewind seek zone
        SeekZoneBox(
            modifier = Modifier
                .fillMaxWidth(0.5F),
            icon = Icons.Default.FastRewind,
            xBackgroundOffset = 0.dp,
            visible = showRewindUi,
            onDoubleClick = {
                onFastRewind(it)
                showRewindUi = true
                coroutineScope.launch {
                    delay(1000L).let { showRewindUi = false }
                }
            },
            onClick = onClick
        )

        // Forward seek zone
        SeekZoneBox(
            modifier = Modifier.fillMaxWidth(1F),
            icon = Icons.Default.FastForward,
            xBackgroundOffset = deviceWidth / 2,
            visible = showForwardUi,
            onDoubleClick = {
                onFastForward(it)
                showForwardUi = true
                coroutineScope.launch {
                    delay(1000L).let { showForwardUi = false }
                }
            },
            onClick = onClick
        )
    }
}

@Composable
fun PlayerBottomBar(
    currentPosition: Long,
    duration: Long,
    model: PlayerViewModel,
    onSliderValueChange: (Long) -> Unit,
) {
    var isValueChanging by remember {
        mutableStateOf(false)
    }
    var currentValue by remember {
        mutableFloatStateOf(0F)
    }

    Box(
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp, 16.dp)
                .fillMaxWidth()
        ) {
            Row {
                Text(
                    text = Util.formatMilliseconds(currentPosition),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.inverseOnSurface
                )

                Text(
                    text = " / ${Util.formatMilliseconds(duration)} (${model.playbackSpeed}x)",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Slider(
                    modifier = Modifier.weight(1f, false),
                    value = if (isValueChanging) currentValue else (currentPosition.toFloat() / duration.toFloat()),
                    enabled = duration != C.TIME_UNSET, // Prevent seeking while content is loading
                    onValueChange = { value ->
                        currentValue = value
                        isValueChanging = true
                    },
                    onValueChangeFinished = {
                        onSliderValueChange((currentValue * duration).toLong())
                        isValueChanging = false
                    },
                    colors = SliderDefaults.colors(
                        inactiveTrackColor = Color.Gray.copy(0.6F),
                        disabledInactiveTrackColor = Color.Gray.copy(0.6F)
                    )
                )

                IconButton(
                    onClick = {
                        model.orientationState =
                            if (model.orientationState == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
                                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            } else {
                                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                            }
                    }
                ) {
                    Icon(
                        imageVector = if (model.orientationState ==
                            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        ) Icons.Rounded.Fullscreen
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

@Composable
fun SeekZoneBox(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    xBackgroundOffset: Dp,
    visible: Boolean,
    onDoubleClick: (offset: Offset) -> Unit,
    onClick: () -> Unit
) {
    val deviceHeight = LocalConfiguration.current.screenHeightDp.dp
    val deviceWidth = LocalConfiguration.current.screenWidthDp.dp

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onDoubleClick(it)
                    },
                    onTap = {
                        onClick()
                    }
                )
            }
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(deviceWidth.div(2))
                    .drawWithCache {
                        onDrawBehind {
                            drawCircle(
                                radius = deviceWidth
                                    .toPx()
                                    .div(2F),
                                color = Color.LightGray,
                                alpha = 0.3F,
                                center = Offset(
                                    xBackgroundOffset.toPx(),
                                    deviceHeight
                                        .toPx()
                                        .div(2F)
                                )
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = icon,
                        tint = Color.White,
                        contentDescription = "rewind"
                    )
                    Text(
                        text = stringResource(id = R.string.instant_seek_time),
                        color = Color.White
                    )
                }
            }
        }
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