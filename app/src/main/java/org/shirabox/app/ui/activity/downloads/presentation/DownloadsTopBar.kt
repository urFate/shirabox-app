package org.shirabox.app.ui.activity.downloads.presentation

import android.app.Activity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import org.shirabox.app.R
import org.shirabox.app.ui.activity.downloads.DownloadsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DownloadsTopBar(currentPage: Int, model: DownloadsViewModel = hiltViewModel()) {
    val activity = LocalContext.current as Activity
    val dropdownExpanded = remember { mutableStateOf(false) }

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
            if (currentPage < 2) {
                IconButton(
                    onClick = { dropdownExpanded.value = true }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        contentDescription = "More"
                    )
                }
            }

            DropdownMenu(
                expanded = dropdownExpanded.value,
                onDismissRequest = { dropdownExpanded.value = false },
            ) {
                DropdownMenuItem(
                    text = {  Text(text = stringResource(R.string.downloads_stop_all)) },
                    onClick = {
                        when (currentPage) {
                            0 -> model.cancelEnqueuedTasks()
                            1 -> model.cancelAllPausedTasks()
                        }
                        dropdownExpanded.value = false
                    }
                )
            }
        }
    )
}