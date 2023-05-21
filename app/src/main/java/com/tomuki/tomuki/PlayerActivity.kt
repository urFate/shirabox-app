package com.tomuki.tomuki

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.tomuki.tomuki.ui.theme.BrandRed
import com.tomuki.tomuki.ui.theme.TomukiTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class PlayerActivity : ComponentActivity() {

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
                    val stream = remember {
                        "https://cache.libria.fun/videos/media/ts/9000/1/480" +
                            "/8a7f4d218433f5a5fee1c6f5a02d278e.m3u8"
                    }
                    val systemUiController = rememberSystemUiController()
                    systemUiController.isSystemBarsVisible = false
                    systemUiController.systemBarsBehavior = WindowInsetsControllerCompat
                        .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

                    VideoPlayer(stream)
                }
            }
        }
    }
}

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

    Box(
        modifier = Modifier.background(Color(0xFF000000))
    ) {
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

        ControlsScaffold(title = "Название", episode = 1, exoPlayer)
    }
}

@Composable
fun ControlsScaffold(title: String, episode: Int, exoPlayer: ExoPlayer){
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

    /**
     * FIXME: Any better solution to update timeline?
     */

    LaunchedEffect(key1 = true) {
        while(true){
            isPlaying = exoPlayer.isPlaying
            totalDuration = exoPlayer.duration
            currentPosition = exoPlayer.contentPosition
            bufferedPercentage = exoPlayer.bufferedPercentage
            playbackState = exoPlayer.playbackState

            delay(400)
        }
    }

    Scaffold(
        topBar = { PlayerTopBar(title, episode) },
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
                onSkipPrevious = { exoPlayer.seekToPrevious() },
                onPlayToggle = {
                    exoPlayer.playWhenReady = !exoPlayer.isPlaying
                },
                onSkipNext = { exoPlayer.seekToNext() }
            )
        },
        containerColor = Color(0x80000000)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerTopBar(title: String, episode: Int){
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
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = null
                )
            }
        },
        actions = {
            IconButton(onClick = { /*TODO*/ }) {
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
    onSkipPrevious: () -> Unit,
    onPlayToggle: () -> Unit,
    onSkipNext: () -> Unit
    ){
    Box(
        modifier = modifier
            .then(
                Modifier.fillMaxSize()
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(48.dp)
        ) {
            IconButton(onClick = onSkipPrevious) {
                Icon(
                    modifier = Modifier.size(42.dp),
                    imageVector = Icons.Outlined.SkipPrevious,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
            IconButton(onClick = onPlayToggle) {
                Icon(
                    modifier = Modifier.size(42.dp),
                    imageVector = if(isPlaying)
                        Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
            IconButton(onClick = onSkipNext) {
                Icon(
                    modifier = Modifier.size(42.dp),
                    imageVector = Icons.Outlined.SkipNext,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.inverseOnSurface
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
){
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
                            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        } else {
                            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        }
                    }
                ) {
                    Icon(
                        imageVector = if(!fullscreen)
                            Icons.Rounded.Fullscreen else Icons.Rounded.FullscreenExit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.inverseOnSurface
                    )
                }
            }
        }
    }
}

private fun formatMilliseconds(timeMs: Long) : String {
    val time = if(timeMs == C.TIME_UNSET) 0 else timeMs

    return time.milliseconds.toComponents { hours, minutes, seconds, _ ->
        "%s%02d:%02d".format(
            if(hours != 0L) "%02d:".format(hours) else "",
            minutes,
            seconds
        )
    }
}