package org.shirabox.app.ui.activity.player.presentation.controls

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.shirabox.app.R

@Composable
internal fun InstantSeekArea(
    seekOffset: Int,
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
            seekOffset = seekOffset,
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
            seekOffset = seekOffset,
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
private fun SeekZoneBox(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    seekOffset: Int,
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
                        text = pluralStringResource(id = R.plurals.seek_plurals, count = seekOffset, seekOffset),
                        color = Color.White
                    )
                }
            }
        }
    }
}