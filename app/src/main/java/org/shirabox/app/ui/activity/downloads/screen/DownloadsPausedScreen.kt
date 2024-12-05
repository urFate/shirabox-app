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
import androidx.compose.material.icons.rounded.PlayArrow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.shirabox.app.ui.activity.downloads.DownloadsViewModel
import org.shirabox.app.ui.activity.downloads.presentation.EnqueuedTeamItem
import org.shirabox.app.ui.activity.downloads.presentation.PausedTaskItem
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach

@Composable
fun DownloadsPausedScreen(model: DownloadsViewModel = hiltViewModel()) {
    val pausedQueryState = model.pausedTasksFlow().collectAsStateWithLifecycle(emptyMap())
    val context = LocalContext.current
    val listState = rememberLazyListState()

    Scaffold(
        floatingActionButton = {
            AnimatedVisibility(
                visible = pausedQueryState.value.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    icon = { Icon(Icons.Rounded.PlayArrow, "Resume All") },
                    text = { Text(text = "Возобновить") },
                    expanded = !listState.canScrollBackward,
                    onClick = {
                        model.resumeTasks(
                            context,
                            *pausedQueryState.value
                                .mapValues { it.value.values.flatten() }
                                .map { entry ->
                                    entry.value.map {
                                        entry.key to it
                                    }
                                }
                                .flatten()
                                .toTypedArray()
                        )
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
                visible = pausedQueryState.value.isEmpty(),
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
                        text = "Очередь пуста. \nМожет скачаем чего-нибудь? paused",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            LazyColumn(
                state = listState
            ) {
                pausedQueryState.value.entries.forEach { contentEntry ->
                    contentEntry.value.forEach { (team, pausedEntities) ->
                        item {
                            val isEmpty = remember(pausedEntities.size) {
                                pausedEntities.isEmpty()
                            }

                            if (!isEmpty) {
                                EnqueuedTeamItem(
                                    modifier = Modifier.padding(0.dp, 16.dp),
                                    title = contentEntry.key.name,
                                    team = team,
                                    tasksAmount = pausedEntities.size,
                                    finished = null
                                ) {
                                    pausedEntities.forEach { entity ->
                                        val episodeFlow = model.episodesFlow(entity.episodeUid)
                                            .collectAsStateWithLifecycle(null)

                                        episodeFlow.value?.let { episode ->
                                            PausedTaskItem(
                                                modifier = Modifier.padding(16.dp, 0.dp),
                                                name = episode.name ?: "Серия ${episode.episode}",
                                                episode = episode.episode,
                                                progress = entity.pausedProgress,
                                                onResume = { model.resumeTasks(context, contentEntry.key to entity) },
                                                onCancel = { model.cancelPausedTasks(contentEntry.key to entity) }
                                            )
                                        }
                                    }
                                }
                                HorizontalDivider(modifier = Modifier.padding(32.dp, 4.dp).fillMaxWidth())
                            }
                        }
                    }
                }
            }
        }
    }
}