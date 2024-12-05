package org.shirabox.app.ui.activity.downloads.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.shirabox.app.R

@Composable
fun EnqueuedTaskItem(
    modifier: Modifier = Modifier,
    episode: Int,
    name: String,
    progress: Float,
    onPause: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier.padding(0.dp, 8.dp).then(modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(weight = 1f, fill = false)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val episodeName = if (name.isBlank()) stringResource(R.string.episode_string, episode) else name

                Text(
                    text = "#$episode",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                Text(
                    text = episodeName,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (progress > 0.001F) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(100)),
                    progress = { progress },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            } else {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(100)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier.padding(16.dp, 0.dp, 0.dp, 0.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPause) {
                Icon(
                    imageVector = Icons.Rounded.Pause,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = "pause"
                )
            }

            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.Outlined.Cancel,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = "cancel"
                )
            }
        }
    }
}