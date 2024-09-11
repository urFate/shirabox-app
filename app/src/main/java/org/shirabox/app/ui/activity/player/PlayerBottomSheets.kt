package org.shirabox.app.ui.activity.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ClosedCaption
import androidx.compose.material.icons.outlined.Hd
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.Sd
import androidx.compose.material.icons.outlined.SlowMotionVideo
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.shirabox.app.R
import org.shirabox.core.entity.EpisodeEntity
import org.shirabox.core.model.Quality
import org.shirabox.core.util.Util

@Composable
fun SettingsBottomSheet(exoPlayer: ExoPlayer, playlist: List<EpisodeEntity>, model: PlayerViewModel) {
    val currentSheetScreen = remember {
        mutableStateOf(SettingsSheetScreen.OVERVIEW)
    }

    if (model.bottomSheetVisibilityState) {
        when (currentSheetScreen.value) {
            SettingsSheetScreen.OVERVIEW -> SettingsOptions(currentSheetScreen, model)
            SettingsSheetScreen.VIDEO_QUALITY -> QualityBottomSheet(
                currentSheetScreen = currentSheetScreen,
                exoPlayer = exoPlayer,
                playlist = playlist,
                model = model
            )

            SettingsSheetScreen.PLAYBACK_SPEED -> PlaybackSpeedBottomSheet(
                currentSheetScreen = currentSheetScreen,
                model = model
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

    val state = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = {
            coroutineScope.launch {
                state.hide()
                model.bottomSheetVisibilityState = false
            }
        },
        contentWindowInsets = { BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Bottom) }
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
                        tint = MaterialTheme.colorScheme.primary,
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
                        tint = MaterialTheme.colorScheme.primary,
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
                        tint = MaterialTheme.colorScheme.primary,
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
    playlist: List<EpisodeEntity>,
    model: PlayerViewModel
) {
    val state = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = {
            hideBottomSheet(coroutineScope, state, currentSheetScreen)
        },
        contentWindowInsets = { BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Bottom) }
    ) {
        Column(
            modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 16.dp)
        ) {
            playlist[exoPlayer.currentMediaItemIndex].videos.keys.reversed().forEach {
                ListItem(
                    modifier = Modifier.clickable {
                        model.currentQuality = it
                        hideBottomSheet(coroutineScope, state, currentSheetScreen)
                    },
                    headlineContent = { Text(
                        when(it) {
                            Quality.FHD -> stringResource(id = R.string.high_quality)
                            Quality.HD -> stringResource(id = R.string.medium_quality)
                            Quality.SD -> stringResource(id = R.string.low_quality)
                        }
                    ) },
                    leadingContent = {
                        Icon(
                            imageVector = when (it) {
                                Quality.FHD -> Icons.Outlined.HighQuality
                                Quality.HD -> Icons.Outlined.Hd
                                Quality.SD -> Icons.Outlined.Sd
                            },
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "${it.quality}"
                        )
                    },
                    supportingContent = { Text("${it.quality}p") }
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
    val state = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = {
            hideBottomSheet(coroutineScope, state, currentSheetScreen)
        },
        contentWindowInsets = { BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Bottom) }
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(0.dp, 0.dp, 0.dp, 16.dp)
            ) {
            val speeds = listOf(0.5F, 1F, 1.5F, 1.75F, 2F, 3F)

            items(speeds) {
                ListItem(
                    modifier = Modifier.clickable {
                        model.playbackSpeed = it
                        hideBottomSheet(coroutineScope, state, currentSheetScreen)
                    },
                    headlineContent = {
                        Text(
                            when (it) {
                                0.5F -> stringResource(id = R.string.slow_playback_speed)
                                1.0F -> stringResource(id = R.string.normal_playback_speed)
                                1.5F -> stringResource(id = R.string.faster_playback_speed)
                                1.75F -> stringResource(id = R.string.bit_faster_playback_speed)
                                2F -> stringResource(id = R.string.fast_playback_speed)
                                3F -> stringResource(id = R.string.very_fast_playback_speed)
                                else -> "Unknown speed"
                            }
                        )
                    },
                    supportingContent = { Text("${it}x") },
                    leadingContent = {
                        Icon(
                            painter = when (it) {
                                0.5F -> painterResource(id = R.drawable.slow_speed)
                                1.0F -> painterResource(id = R.drawable.normal_speed)
                                1.5F -> painterResource(id = R.drawable.faster_speed)
                                1.75F -> painterResource(id = R.drawable.bit_faster_speed)
                                2.0F -> painterResource(id = R.drawable.fast_speed)
                                3.0F -> painterResource(id = R.drawable.very_fast_speed)
                                else -> painterResource(id = R.drawable.normal_speed)
                            },
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "${it}x"
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClosedCationsBottomSheet(currentSheetScreen: MutableState<SettingsSheetScreen>) {
    val state = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = {
            coroutineScope.launch {
                state.hide()
                currentSheetScreen.value = SettingsSheetScreen.OVERVIEW
            }
        },
        contentWindowInsets = { BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Bottom) }
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(modifier = Modifier.padding(96.dp), text = "В разработке...")
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