package live.shirabox.shirabox.ui.activity.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import live.shirabox.core.datastore.DataStoreScheme
import live.shirabox.core.util.Util
import live.shirabox.core.util.Values
import live.shirabox.shirabox.R

@Composable
fun ControlsScaffold(exoPlayer: ExoPlayer, model: PlayerViewModel) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isPlaying by remember { mutableStateOf(exoPlayer.isPlaying) }
    var currentPosition by remember { mutableLongStateOf(exoPlayer.currentPosition) }
    var totalDuration by remember { mutableLongStateOf(exoPlayer.duration) }
    var playbackState by remember { mutableIntStateOf(exoPlayer.playbackState) }
    var hasNextMediaItem by remember { mutableStateOf(model.playlist.lastIndex != exoPlayer.currentMediaItemIndex) }
    var hasPreviousMediaItem by remember { mutableStateOf(exoPlayer.hasPreviousMediaItem()) }
    var currentMediaItemIndex by remember { mutableIntStateOf(exoPlayer.currentMediaItemIndex) }

    val currentEpisode = remember(currentMediaItemIndex) {
        model.playlist[exoPlayer.currentMediaItemIndex].episode
    }

    val providedIntroMarkers = remember(currentMediaItemIndex) {
        model.playlist[exoPlayer.currentMediaItemIndex].openingMarkers
    }
    val animeSkipIntroMarkers = remember(currentMediaItemIndex) {
        model.animeSkipTimestamps[currentEpisode]
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

    val openingSkipPreferenceFlow =
        model.openingSkipPreferenceFlow(LocalContext.current).collectAsState(
            initial = DataStoreScheme.FIELD_OPENING_SKIP.defaultValue
        )
    val openingAutoSkip = remember(openingSkipPreferenceFlow) {
        openingSkipPreferenceFlow.value ?: DataStoreScheme.FIELD_OPENING_SKIP.defaultValue
    }


    val activity = LocalContext.current as Activity

    activity.requestedOrientation = model.orientationState

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
            hasNextMediaItem = model.playlist.lastIndex != exoPlayer.currentMediaItemIndex
            hasPreviousMediaItem = exoPlayer.hasPreviousMediaItem()
            currentMediaItemIndex = exoPlayer.currentMediaItemIndex.inc()

            delay(200)
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
                    model = model,
                    onSliderValueChange = {
                        model.controlsVisibilityState = true
                    }
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
                    onSkipPrevious = {
                        model.saveEpisodePosition(currentEpisode, exoPlayer.currentPosition)
                        exoPlayer.seekToPrevious()
                    },
                    onPlayToggle = {
                        exoPlayer.playWhenReady = !exoPlayer.isPlaying
                        coroutineScope.launch { hideControls(exoPlayer, model) }
                    },
                    onSkipNext = {
                        model.saveEpisodePosition(currentEpisode, exoPlayer.currentPosition)
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

        SkipButton(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerTopBar(title: String, episode: Int, onSettingsClick: () -> Unit) {
    val activity = LocalContext.current as Activity

    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        title = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    text = title,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                Text(
                    text = stringResource(id = R.string.episode_string, episode),
                    color = Color.White.copy(0.7f),
                    fontSize = 14.sp
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = { activity.finish() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
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
            CircularProgressIndicator(
                strokeCap = StrokeCap.Round
            )
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
                    imageVector = Icons.Rounded.SkipPrevious,
                    isActive = hasPreviousMediaItem,
                    onClick = onSkipPrevious
                )
                PlaybackIconButton(
                    imageVector = if (isPlaying)
                        Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    onClick = onPlayToggle
                )
                PlaybackIconButton(
                    imageVector = Icons.Rounded.SkipNext,
                    isActive = hasNextMediaItem,
                    onClick = onSkipNext
                )
            }
        }
    }
}

@Composable
fun SkipButton(
    autoSkip: Boolean,
    isPlaying: Boolean,
    onTimeout: () -> Unit,
    onClick: () -> Unit
) {
    var percentage by remember { mutableIntStateOf(0) }
    val orientation = LocalConfiguration.current.orientation
    val isVerticalOrientation =
        orientation == Configuration.ORIENTATION_PORTRAIT || orientation == Configuration.ORIENTATION_UNDEFINED
    val contentAlignment = if (isVerticalOrientation) Alignment.CenterEnd else Alignment.BottomEnd

    if (autoSkip) {
        LaunchedEffect(isPlaying) {
            if (isPlaying && percentage >= 0) {
                while (percentage < 100) {
                    percentage++
                    delay(40L)
                }
                onTimeout()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp, 64.dp),
        contentAlignment = contentAlignment
    ) {
        val endPadding = if (isVerticalOrientation) 0.dp else 16.dp

        OutlinedButton(
            modifier = Modifier.padding(0.dp, 128.dp, endPadding, 0.dp),
            border = BorderStroke(1.dp, Color.White),
            shape = RoundedCornerShape(40),
            contentPadding = PaddingValues(0.dp),
            onClick = onClick
        ) {
            Row(
                modifier = Modifier
                    .drawBehind {
                        if (autoSkip) {
                            drawRoundRect(
                                color = Color.Gray.copy(alpha = 0.5f),
                                size = size.copy(
                                    size.width
                                        .div(100)
                                        .times(percentage)
                                )
                            )
                        }
                    }
                    .padding(18.dp, 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = if (autoSkip) R.string.watch else R.string.opening_skip),
                    color = Color.White
                )
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

        var showRewindUi by remember { mutableStateOf(false) }
        var showForwardUi by remember { mutableStateOf(false) }

        LaunchedEffect(showRewindUi) {
            if(showRewindUi) delay(1000L).let { showRewindUi = false }
        }
        LaunchedEffect(showForwardUi) {
            if(showForwardUi) delay(1000L).let { showForwardUi = false }
        }

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
    onSliderValueChange: () -> Unit,
    onSliderValueChangeFinish: (Long) -> Unit,
) {
    var isValueChanging by remember { mutableStateOf(false) }
    var currentValue by remember { mutableFloatStateOf(0F) }
    var mutablePosition by remember {
        mutableLongStateOf(currentPosition)
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
                    text = Util.formatMilliseconds(
                        if(isValueChanging) mutablePosition else currentPosition
                    ),
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
                        mutablePosition = value.times(duration).toLong()
                        currentValue = value
                        isValueChanging = true
                        onSliderValueChange()
                    },
                    onValueChangeFinished = {
                        onSliderValueChangeFinish((currentValue * duration).toLong())
                        isValueChanging = false
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.onPrimary,
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
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f)
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
                    onDoubleTap = onDoubleClick,
                    onTap = { onClick() },
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