package org.shirabox.app.ui.activity.player.presentation.controls

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.shirabox.app.R

@Composable
internal fun PlaybackControls(
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
                    imageResource = R.drawable.player_skip_back,
                    isActive = hasPreviousMediaItem,
                    onClick = onSkipPrevious
                )
                PlaybackIconButton(
                    imageResource = if (isPlaying)
                        R.drawable.pause_filled else R.drawable.play_filled,
                    onClick = onPlayToggle
                )
                PlaybackIconButton(
                    imageResource = R.drawable.player_skip_forward,
                    isActive = hasNextMediaItem,
                    onClick = onSkipNext
                )
            }
        }
    }
}

@Composable
private fun PlaybackIconButton(
    isActive: Boolean = true,
    imageResource: Int,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(100))
            .clickable(
                interactionSource = interactionSource,
                enabled = isActive,
                indication = ripple(bounded = true, radius = 100.dp, color = Color.White),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(32.dp),
            painter = painterResource(imageResource),
            contentDescription = null,
            tint = if(isActive) MaterialTheme.colorScheme.inverseOnSurface
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f)
        )
    }
}