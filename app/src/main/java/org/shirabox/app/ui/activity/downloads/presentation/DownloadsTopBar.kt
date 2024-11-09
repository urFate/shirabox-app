package org.shirabox.app.ui.activity.downloads.presentation

import android.app.Activity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.shirabox.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DownloadsTopBar() {
    val activity = LocalContext.current as Activity

    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        title = {
            Text(stringResource(R.string.downloads_activity_title))
        },
        navigationIcon = {
            IconButton(
                onClick = activity::finish
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = "back"
                )
            }
        },
        actions = {
            IconButton(
                onClick = {  }
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = "back"
                )
            }
        }
    )
}