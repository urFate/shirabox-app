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
import androidx.compose.foundation.pager.PagerState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.shirabox.app.R
import org.shirabox.app.ui.activity.downloads.DownloadsViewModel
import org.shirabox.app.ui.activity.downloads.presentation.EnqueuedTeamItem
import org.shirabox.app.ui.activity.downloads.presentation.PausedTaskItem
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach

@Composable
fun DownloadsPausedScreen(pagerState: PagerState, model: DownloadsViewModel = hiltViewModel()) {
    val pausedQueryState = model.pausedTasksFlow().collectAsStateWithLifecycle(emptyMap())
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
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
                    text = { Text(text = stringResource(R.string.downloads_resume)) },
                    expanded = !listState.canScrollBackward,
                    onClick = {
                        model.resumeAllTasks(context)
                        coroutineScope.launch { pagerState.scrollToPage(0) }
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
                        text = stringResource(R.string.downloads_empty_query),
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
                                    pausedEntities.forEach { downloadEntity ->
                                        val episodeFlow = model.episodesFlow(downloadEntity.episodeUid)
                                            .collectAsStateWithLifecycle(null)

                                        episodeFlow.value?.let { episodeEntity ->
                                            PausedTaskItem(
                                                modifier = Modifier.padding(16.dp, 0.dp),
                                                name = episodeEntity.name ?: stringResource(R.string.episode_string, episodeEntity.episode),
                                                episode = episodeEntity.episode,
                                                progress = downloadEntity.pausedProgress,
                                                onResume = { model.resumeTasks(context, contentEntry.key to downloadEntity) },
                                                onCancel = { model.cancelPausedTasks(contentEntry.key to downloadEntity) }
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