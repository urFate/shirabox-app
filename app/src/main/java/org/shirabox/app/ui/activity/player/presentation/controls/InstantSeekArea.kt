package org.shirabox.app.ui.activity.player.presentation.controls

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.shirabox.app.R

@Composable
internal fun InstantSeekArea(
    seekOffset: Int,
    onFastRewind: (multiplier: Int) -> Unit,
    onFastForward: (multiplier: Int) -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val deviceWidth = LocalConfiguration.current.screenWidthDp.dp

        val fastRewindMultiplier = remember { mutableIntStateOf(0) }
        var fastRewindActivated by remember { mutableStateOf(false) }

        val fastForwardMultiplier = remember { mutableIntStateOf(0) }
        var fastForwardActivated by remember { mutableStateOf(false) }

        val userInputAwaitTime = 800L

        LaunchedEffect(fastRewindMultiplier.intValue, fastForwardMultiplier.intValue) {
            if (fastRewindActivated) {
                delay(userInputAwaitTime)
                onFastRewind(fastRewindMultiplier.intValue)
            }

            if (fastForwardActivated) {
                delay(userInputAwaitTime)
                onFastForward(fastForwardMultiplier.intValue)
            }

            fastRewindActivated = false
            fastForwardActivated = false
            delay(100L)
            fastRewindMultiplier.intValue = 0
            fastForwardMultiplier.intValue = 0
        }

        // Rewind seek zone
        SeekZoneBox(
            modifier = Modifier
                .fillMaxWidth(0.5F),
            icon = Icons.Default.FastRewind,
            seekOffset = seekOffset.times(fastRewindMultiplier.intValue),
            xBackgroundOffset = 0.dp,
            xSlideOffset = -150,
            visible = fastRewindActivated,
            onDoubleClick = {
                fastRewindActivated = true
                if (fastRewindMultiplier.intValue <= 4) {
                    fastRewindMultiplier.intValue += 1
                }
            },
            onClick = {
                 if (!fastRewindActivated) onClick()
            }
        )

        // Forward seek zone
        SeekZoneBox(
            modifier = Modifier.fillMaxWidth(1F),
            icon = Icons.Default.FastForward,
            seekOffset = seekOffset.times(fastForwardMultiplier.intValue),
            xBackgroundOffset = deviceWidth / 2,
            xSlideOffset = 150,
            visible = fastForwardActivated,
            onDoubleClick = {
                fastForwardActivated = true
                if (fastForwardMultiplier.intValue <= 4) {
                    fastForwardMultiplier.intValue += 1
                }
            },
            onClick = {
                if (!fastForwardActivated) onClick()
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SeekZoneBox(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    seekOffset: Int,
    xBackgroundOffset: Dp,
    xSlideOffset: Int,
    visible: Boolean,
    onDoubleClick: () -> Unit,
    onClick: () -> Unit
) {
    val deviceHeight = LocalConfiguration.current.screenHeightDp.dp
    val deviceWidth = LocalConfiguration.current.screenWidthDp.dp

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .combinedClickable(
                enabled = true,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onDoubleClick = onDoubleClick
            )
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(
                animationSpec = tween(durationMillis = 180)
            ) + slideIn(
                initialOffset = { IntOffset(xSlideOffset, 0) }
            ),
            exit = fadeOut(
                animationSpec = tween(durationMillis = 180)
            ) + slideOut(
                animationSpec = spring(
                    stiffness = Spring.StiffnessMedium,
                    visibilityThreshold = IntOffset.VisibilityThreshold
                ),
                targetOffset = { IntOffset(xSlideOffset, 0) }),
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
                                alpha = 0.2F,
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