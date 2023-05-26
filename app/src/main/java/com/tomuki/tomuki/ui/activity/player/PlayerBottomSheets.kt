package com.tomuki.tomuki.ui.activity.player

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ClosedCaption
import androidx.compose.material.icons.outlined.Sd
import androidx.compose.material.icons.outlined.SlowMotionVideo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.tomuki.tomuki.R
import com.tomuki.tomuki.util.Util
import kotlinx.coroutines.launch

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
        Util.hideSystemUi(systemUiController)
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

enum class SettingsSheetScreen {
    OVERVIEW, VIDEO_QUALITY, PLAYBACK_SPEED, CLOSED_CAPTIONS
}