package com.tomuki.tomuki.ui.activity.player

import android.app.Activity
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
import androidx.compose.material.icons.outlined.ClosedCaption
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Sd
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.SlowMotionVideo
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.tomuki.tomuki.R
import com.tomuki.tomuki.ui.theme.BrandRed
import com.tomuki.tomuki.ui.theme.TomukiTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

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

                    hideSystemUi(systemUiController)

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

@Composable
fun ControlsScaffold(
    title: String, episode: Int, exoPlayer: ExoPlayer,
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
    val coroutineScope = rememberCoroutineScope()

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
                duration = totalDuration
            ) {
                exoPlayer.seekTo(it)
            }
        },
        content = {
            PlaybackControls(
                modifier = Modifier.padding(it),
                isPlaying = isPlaying,
                isLoaded = playbackState == Player.STATE_READY,
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
                    onClick = onSkipPrevious
                )
                PlaybackIconButton(
                    imageVector = if (isPlaying)
                        Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                    onClick = onPlayToggle
                )
                PlaybackIconButton(
                    imageVector = Icons.Outlined.SkipNext,
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
    onSliderValueChange: (Long) -> Unit,
) {
    var fullscreen by rememberSaveable {
        mutableStateOf(false)
    }
    val activity = LocalContext.current as Activity

    Box(
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp, 16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "${formatMilliseconds(currentPosition)} • ${formatMilliseconds(duration)}",
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
                        fullscreen = !fullscreen

                        if (fullscreen) {
                            activity.requestedOrientation =
                                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        } else {
                            activity.requestedOrientation =
                                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (!fullscreen)
                            Icons.Rounded.Fullscreen else Icons.Rounded.FullscreenExit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.inverseOnSurface
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsBottomSheet(visibilityState: MutableState<Boolean>) {
    val currentSheetScreen = remember {
        mutableStateOf(SettingsSheetScreen.OVERVIEW)
    }

    if (visibilityState.value) {
        when (currentSheetScreen.value) {
            SettingsSheetScreen.OVERVIEW -> SettingsOptions(currentSheetScreen, visibilityState)
            SettingsSheetScreen.VIDEO_QUALITY -> QualityBottomSheet(currentSheetScreen)
            SettingsSheetScreen.PLAYBACK_SPEED -> PlaybackSpeedBottomSheet(currentSheetScreen)
            SettingsSheetScreen.CLOSED_CAPTIONS -> ClosedCationsBottomSheet(currentSheetScreen)
        }
    } else {

        /**
         * We have to hide the system UI due to the
         * fact that the BottomSheet shows it when opening
         */

        val systemUiController = rememberSystemUiController()
        hideSystemUi(systemUiController)
    }

}

@Composable
fun PlaybackIconButton(imageVector: ImageVector, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            modifier = Modifier.size(42.dp),
            imageVector = imageVector,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.inverseOnSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsOptions(
    currentSheetScreen: MutableState<SettingsSheetScreen>,
    visibilityState: MutableState<Boolean>
) {

    val skipPartiallyExpanded by remember { mutableStateOf(false) }
    val state = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = {
            coroutineScope.launch {
                state.hide()
                visibilityState.value = false
            }
        }
    ) {

        /*
         * Video Quality
         */

        ListItem(
            modifier = Modifier.clickable {
                coroutineScope.launch {
                    state.hide()
                    currentSheetScreen.value = SettingsSheetScreen.VIDEO_QUALITY
                }
            },
            headlineContent = { Text(stringResource(id = R.string.quality, 480)) },
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.Sd,
                    contentDescription = null
                )
            }
        )

        /*
         * Playback Speed
         */

        ListItem(
            modifier = Modifier.clickable {
                coroutineScope.launch {
                    state.hide()
                    currentSheetScreen.value = SettingsSheetScreen.PLAYBACK_SPEED
                }
            },
            headlineContent = { Text(stringResource(id = R.string.playback_speed)) },
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.SlowMotionVideo,
                    contentDescription = null
                )
            }
        )

        /*
         * Subtitles
         */

        ListItem(
            modifier = Modifier.clickable {
                coroutineScope.launch {
                    state.hide()
                    currentSheetScreen.value = SettingsSheetScreen.CLOSED_CAPTIONS
                }
            },
            headlineContent = { Text(stringResource(id = R.string.subtitles)) },
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.ClosedCaption,
                    contentDescription = null
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QualityBottomSheet(currentSheetScreen: MutableState<SettingsSheetScreen>) {
    val skipPartiallyExpanded by remember { mutableStateOf(false) }
    val state = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = {
            coroutineScope.launch {
                state.hide()
            }
            currentSheetScreen.value = SettingsSheetScreen.OVERVIEW
        }
    ) {
        // TODO: Loop available qualities from API

        ListItem(
            modifier = Modifier.clickable { /* TODO */ },
            headlineContent = { Text("1080p") },
        )
        ListItem(
            modifier = Modifier.clickable { /* TODO */ },
            headlineContent = { Text("720p") },
        )
        ListItem(
            modifier = Modifier.clickable { /* TODO */ },
            headlineContent = { Text("480p") },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackSpeedBottomSheet(currentSheetScreen: MutableState<SettingsSheetScreen>) {
    val skipPartiallyExpanded by remember { mutableStateOf(false) }
    val state = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = {
            coroutineScope.launch {
                state.hide()
                currentSheetScreen.value = SettingsSheetScreen.OVERVIEW
            }
        }
    ) {
        var speed = 0.25
        for (i in 1..7) {
            val text = if (speed == 0.0) stringResource(id = R.string.normal_speed) else speed
            ListItem(
                modifier = Modifier.clickable { /* TODO */ },
                headlineContent = { Text("$text") },
            )
            speed += 0.25
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClosedCationsBottomSheet(currentSheetScreen: MutableState<SettingsSheetScreen>) {
    val skipPartiallyExpanded by remember { mutableStateOf(false) }
    val state = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = {
            coroutineScope.launch {
                state.hide()
                currentSheetScreen.value = SettingsSheetScreen.OVERVIEW
            }
        }
    ) {
        Text("В разработке")
    }
}

private suspend fun hideControls(
    exoPlayer: ExoPlayer,
    state: MutableState<Boolean>
) {
    delay(3000).let {
        if (exoPlayer.isPlaying) state.value = false
    }
}

private fun hideSystemUi(controller: SystemUiController) {
    controller.isSystemBarsVisible = false
    controller.systemBarsBehavior = WindowInsetsControllerCompat
        .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
}

private fun formatMilliseconds(timeMs: Long): String {
    val time = if (timeMs == C.TIME_UNSET) 0 else timeMs

    return time.milliseconds.toComponents { hours, minutes, seconds, _ ->
        "%s%02d:%02d".format(
            if (hours != 0L) "%02d:".format(hours) else "",
            minutes,
            seconds
        )
    }
}

enum class SettingsSheetScreen {
    OVERVIEW, VIDEO_QUALITY, PLAYBACK_SPEED, CLOSED_CAPTIONS
}