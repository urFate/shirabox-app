package org.shirabox.app.ui.activity.player.presentation.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.shirabox.app.R
import org.shirabox.core.model.Quality

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlayerTopBar(title: String, episode: Int, offlineQuality: Quality?, onBackClick: () -> Unit, onSettingsClick: () -> Unit) {
    TopAppBar(
        modifier = Modifier
            .padding(4.dp, 16.dp)
            .fillMaxWidth(),
        title = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    text = title,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.episode_string, episode),
                        color = Color.White.copy(0.7f),
                        fontSize = 14.sp
                    )

                    val qualityIcon = remember(offlineQuality) {
                        when (offlineQuality) {
                            Quality.SD -> R.drawable.badge_sd
                            Quality.HD -> R.drawable.badge_hd
                            Quality.FHD -> R.drawable.badge_fhd
                            null -> R.drawable.badge_sd
                        }
                    }

                    if (offlineQuality != null) {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            painter = painterResource(qualityIcon),
                            tint = Color.White.copy(0.7f),
                            contentDescription = "quality"
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(R.drawable.arrow_left),
                    contentDescription = null
                )
            }
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    painter = painterResource(R.drawable.cog),
                    contentDescription = null
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0x00000000),
            navigationIconContentColor = MaterialTheme.colorScheme.inverseOnSurface,
            titleContentColor = MaterialTheme.colorScheme.inverseOnSurface,
            actionIconContentColor = MaterialTheme.colorScheme.inverseOnSurface,
        )
    )
}