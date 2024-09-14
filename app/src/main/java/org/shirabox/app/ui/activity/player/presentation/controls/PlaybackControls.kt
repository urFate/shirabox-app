package org.shirabox.app.ui.activity.player.presentation.controls

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

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
                    imageVector = Icons.Rounded.SkipPrevious,
                    isActive = hasPreviousMediaItem,
                    onClick = onSkipPrevious
                )
                PlaybackIconButton(
                    imageVector = if (isPlaying)
                        Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    onClick = onPlayToggle
                )
                PlaybackIconButton(
                    imageVector = Icons.Rounded.SkipNext,
                    isActive = hasNextMediaItem,
                    onClick = onSkipNext
                )
            }
        }
    }
}

@Composable
private fun PlaybackIconButton(isActive: Boolean = true, imageVector: ImageVector, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        enabled = isActive
    ) {
        Icon(
            modifier = Modifier.size(42.dp),
            imageVector = imageVector,
            contentDescription = null,
            tint = if(isActive) MaterialTheme.colorScheme.inverseOnSurface
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f)
        )
    }
}