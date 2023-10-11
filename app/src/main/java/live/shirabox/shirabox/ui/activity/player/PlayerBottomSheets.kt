package live.shirabox.shirabox.ui.activity.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ClosedCaption
import androidx.compose.material.icons.outlined.Hd
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.Sd
import androidx.compose.material.icons.outlined.SlowMotionVideo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import live.shirabox.core.model.Quality
import live.shirabox.core.util.Util
import live.shirabox.shirabox.R

@Composable
fun SettingsBottomSheet(exoPlayer: ExoPlayer, model: PlayerViewModel) {
    val currentSheetScreen = remember {
        mutableStateOf(SettingsSheetScreen.OVERVIEW)
    }

    if (model.bottomSheetVisibilityState) {
        when (currentSheetScreen.value) {
            SettingsSheetScreen.OVERVIEW -> SettingsOptions(currentSheetScreen, model)
            SettingsSheetScreen.VIDEO_QUALITY -> QualityBottomSheet(
                currentSheetScreen,
                exoPlayer,
                model
            )

            SettingsSheetScreen.PLAYBACK_SPEED -> PlaybackSpeedBottomSheet(
                currentSheetScreen,
                model
            )

            SettingsSheetScreen.CLOSED_CAPTIONS -> ClosedCationsBottomSheet(currentSheetScreen)
        }
    } else {

        /**
         * We have to hide the system UI due to the
         * fact that the BottomSheet shows it when opening
         */

        val systemUiController = rememberSystemUiController()
        Util.hideSystemUi(systemUiController)
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsOptions(
    currentSheetScreen: MutableState<SettingsSheetScreen>,
    model: PlayerViewModel
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
                model.bottomSheetVisibilityState = false
            }
        }
    ) {
        Column(
            modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 16.dp)
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
                headlineContent = {
                    Text(
                        stringResource(
                            id = R.string.quality,
                            model.currentQuality.quality
                        )
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = when (model.currentQuality) {
                            Quality.SD -> Icons.Outlined.Sd
                            Quality.HD -> Icons.Outlined.Hd
                            Quality.FHD -> Icons.Outlined.HighQuality
                        },
                        contentDescription = model.currentQuality.quality.toString()
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
                headlineContent = {
                    Text(
                        stringResource(
                            id = R.string.playback_speed,
                            model.playbackSpeed
                        )
                    )
                },
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QualityBottomSheet(
    currentSheetScreen: MutableState<SettingsSheetScreen>,
    exoPlayer: ExoPlayer,
    model: PlayerViewModel
) {
    val skipPartiallyExpanded by remember { mutableStateOf(false) }
    val state = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = {
            hideBottomSheet(coroutineScope, state, currentSheetScreen)
        }
    ) {
        Column(
            modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 16.dp)
        ) {
            model.playlist[exoPlayer.currentMediaItemIndex].streamUrls.keys.forEach {
                ListItem(
                    modifier = Modifier.clickable {
                        model.currentQuality = it
                        hideBottomSheet(coroutineScope, state, currentSheetScreen)
                    },
                    headlineContent = { Text("${it.quality}p") },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackSpeedBottomSheet(
    currentSheetScreen: MutableState<SettingsSheetScreen>,
    model: PlayerViewModel
) {
    val skipPartiallyExpanded by remember { mutableStateOf(false) }
    val state = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = {
            hideBottomSheet(coroutineScope, state, currentSheetScreen)
        }
    ) {
        Column(
            modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 16.dp)

        ) {
            val speeds = listOf(0.5F, 1F, 1.5F, 2F, 2.5F, 3F)

            speeds.forEach {
                ListItem(
                    modifier = Modifier.clickable {
                        model.playbackSpeed = it
                        hideBottomSheet(coroutineScope, state, currentSheetScreen)
                    },
                    headlineContent = { Text("${it}x") },
                )
            }
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
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(modifier = Modifier.padding(128.dp), text = "В разработке...")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
internal fun hideBottomSheet(
    scope: CoroutineScope,
    state: SheetState,
    currentScreen: MutableState<SettingsSheetScreen>
) {
    scope.launch {
        state.hide()
    }
    currentScreen.value = SettingsSheetScreen.OVERVIEW
}

enum class SettingsSheetScreen {
    OVERVIEW, VIDEO_QUALITY, PLAYBACK_SPEED, CLOSED_CAPTIONS
}