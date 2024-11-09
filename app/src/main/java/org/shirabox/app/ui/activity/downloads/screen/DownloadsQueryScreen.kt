package org.shirabox.app.ui.activity.downloads.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAddCheck
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.shirabox.app.ui.activity.downloads.DownloadsViewModel
import org.shirabox.app.ui.activity.downloads.presentation.EnqueuedTeamItem

@Composable
fun DownloadsQueryScreen(model: DownloadsViewModel = hiltViewModel()) {
    val queryState = model.queryFlow().collectAsStateWithLifecycle(emptyMap())
    val listState = rememberLazyListState()

    Scaffold(
        floatingActionButton = {
            AnimatedVisibility(
                visible = queryState.value.isNotEmpty()
            ) {
                ExtendedFloatingActionButton(
                    icon = { Icon(Icons.Filled.Pause, "Pause") },
                    text = { Text(text = "Приостановить") },
                    expanded = !listState.canScrollBackward,
                    onClick = {
                        model.pauseQuery()
                    }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopStart
        ) {
            AnimatedVisibility(
                visible = queryState.value.isEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(0.dp, 128.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(64.dp),
                        imageVector = Icons.AutoMirrored.Rounded.PlaylistAddCheck,
                        tint = MaterialTheme.colorScheme.outline,
                        contentDescription = "Fresh"
                    )
                    Text(
                        text = "Очередь пуста. \nМожет скачаем чего-нибудь?",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            LazyColumn(
                state = listState
            ) {
                queryState.value.entries.forEach { contentEntry ->
                    contentEntry.value.forEach { (team, enqueuedTasks) ->
                        item {
                            val isEmpty = remember(enqueuedTasks.size) {
                                enqueuedTasks.isEmpty()
                            }

                            if (!isEmpty) {
                                Column {
                                    EnqueuedTeamItem(
                                        modifier = Modifier.padding(0.dp, 16.dp),
                                        title = contentEntry.key.name,
                                        team = team,
                                        tasks = enqueuedTasks
                                    )

                                    HorizontalDivider(modifier = Modifier.padding(32.dp, 4.dp).fillMaxWidth())
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}