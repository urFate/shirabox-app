package org.shirabox.app.ui.activity.player.presentation.controls

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.C
import org.shirabox.app.ui.activity.player.PlayerViewModel
import org.shirabox.core.util.Util

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlayerBottomBar(
    currentPosition: Long,
    duration: Long,
    model: PlayerViewModel,
    onSliderValueChange: () -> Unit,
    onSliderValueChangeFinish: (Long) -> Unit,
) {
    var isValueChanging by remember { mutableStateOf(false) }
    var currentValue by remember { mutableFloatStateOf(0F) }
    var mutablePosition by remember { mutableLongStateOf(currentPosition) }

    val animatedValue by animateFloatAsState(if (isValueChanging) currentValue else (currentPosition.toFloat() / duration.toFloat()),
        label = ""
    )

    val thumbGapSize by animateDpAsState(if (isValueChanging) 8.dp else 0.dp, label = "")
    val cornerSize by animateDpAsState(if (isValueChanging) 4.dp else 0.dp, label = "")
    val thumbHeight by animateDpAsState(if (isValueChanging) 36.dp else 16.dp, label = "")

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
                val colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.onPrimary,
                    inactiveTrackColor = Color.Gray.copy(0.4F),
                    disabledInactiveTrackColor = Color.Gray.copy(0.4F)
                )
                val interactionSource = remember(::MutableInteractionSource)

                Slider(
                    modifier = Modifier.weight(1f, false),
                    value = animatedValue,
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
                    track = {
                        SliderDefaults.Track(
                            colors = colors,
                            enabled = duration != C.TIME_UNSET,
                            sliderState = it,
                            thumbTrackGapSize = thumbGapSize,
                            trackInsideCornerSize = cornerSize
                        )
                    },
                    thumb = {
                        SliderDefaults.Thumb(
                            interactionSource = interactionSource,
                            colors = colors,
                            enabled = true,
                            thumbSize = DpSize(6.dp, thumbHeight)
                        )
                    },
                    colors = colors
                )

                val activity = LocalContext.current as Activity

                val isPortrait = when (activity.requestedOrientation) {
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED,
                    ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT,
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> true

                    else -> false
                }

                IconButton(
                    onClick = {
                        activity.requestedOrientation =
                            if (isPortrait) {
                                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            } else {
                                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            }
                    }
                ) {
                    Icon(
                        imageVector = if (isPortrait) {
                            Icons.Rounded.Fullscreen
                        } else {
                            Icons.Rounded.FullscreenExit
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.inverseOnSurface
                    )
                }
            }
        }
    }
}