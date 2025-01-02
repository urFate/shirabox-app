package org.shirabox.app.ui.activity.player.presentation

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
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.shirabox.app.R
import org.shirabox.app.ui.activity.player.PlayerViewModel
import org.shirabox.core.entity.EpisodeEntity
import org.shirabox.core.model.Quality

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
        contentWindowInsets = {
            BottomSheetDefaults.windowInsets
                .only(WindowInsetsSides.Bottom)
                .only(WindowInsetsSides.Top)
        }
    ) {
        Column(
            modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 16.dp)
        ) {
            /*
             * Video Quality
             */

            val isCurrentItemOffline = remember {
                derivedStateOf { model.isCurrentItemOffline.value }
            }

            ListItem(
                modifier = Modifier.clickable(
                    enabled = !isCurrentItemOffline.value
                ) {
                    coroutineScope.launch {
                        state.hide()
                        currentSheetScreen.value = SettingsSheetScreen.VIDEO_QUALITY
                    }
                },
                headlineContent = {
                    Text(
                        if (model.isCurrentItemOffline.value) {
                            stringResource(id = R.string.quality_offline)
                        } else {
                            stringResource(id = R.string.quality, model.currentQuality.quality)
                        }
                    )
                },
                leadingContent = {
                    val qualityIcon = remember(model.currentQuality, model.isCurrentItemOffline.value) {
                        if (!model.isCurrentItemOffline.value) {
                            when (model.currentQuality) {
                                Quality.SD -> R.drawable.badge_sd
                                Quality.HD -> R.drawable.badge_hd
                                Quality.FHD -> R.drawable.badge_fhd
                            }
                        } else {
                            R.drawable.wifi_slash
                        }
                    }

                    Icon(
                        painter = painterResource(qualityIcon),
                        tint = if (!model.isCurrentItemOffline.value) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color.Gray
                        },
                        contentDescription = model.currentQuality.quality.toString()
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
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
                        painter = painterResource(R.drawable.speed),
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
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
                        painter = painterResource(R.drawable.closed_captions),
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
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
                        val icon = remember {
                            when (it) {
                                Quality.SD -> R.drawable.badge_sd
                                Quality.HD -> R.drawable.badge_hd
                                Quality.FHD -> R.drawable.badge_fhd
                            }
                        }

                        Icon(
                            painter = painterResource(icon),
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "${it.quality}"
                        )
                    },
                    supportingContent = { Text("${it.quality}p") },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
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
                                0.5F -> painterResource(id = R.drawable.multiplier_0_5x)
                                1.0F -> painterResource(id = R.drawable.multiplier_1x)
                                1.5F -> painterResource(id = R.drawable.multiplier_1_5x)
                                1.75F -> painterResource(id = R.drawable.multiplier_0_75x)
                                2.0F -> painterResource(id = R.drawable.multiplier_2x)
                                3.0F -> painterResource(id = R.drawable.multiplier_3x)
                                else -> painterResource(id = R.drawable.multiplier_1x)
                            },
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "${it}x"
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
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