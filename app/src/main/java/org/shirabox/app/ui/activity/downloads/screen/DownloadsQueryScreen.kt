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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.shirabox.app.R
import org.shirabox.app.service.media.model.EnqueuedTask
import org.shirabox.app.service.media.model.TaskState
import org.shirabox.app.ui.activity.downloads.DownloadsViewModel
import org.shirabox.app.ui.activity.downloads.presentation.EnqueuedTaskItem
import org.shirabox.app.ui.activity.downloads.presentation.EnqueuedTeamItem
import org.shirabox.core.model.Content

@Composable
fun DownloadsQueryScreen(pagerState: PagerState, model: DownloadsViewModel = hiltViewModel()) {
    val queryState = model.sortedQueryFlow().collectAsStateWithLifecycle(emptyMap())
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    Scaffold(
        floatingActionButton = {
            AnimatedVisibility(
                visible = queryState.value.isNotEmpty()
            ) {
                ExtendedFloatingActionButton(
                    icon = { Icon(Icons.Filled.Pause, "Pause") },
                    text = { Text(text = stringResource(R.string.downloads_pause)) },
                    expanded = !listState.canScrollBackward,
                    onClick = {
                        model.pauseQuery()
                        coroutineScope.launch { pagerState.scrollToPage(1) }
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 128.dp),
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
                queryState.value.entries.forEach { contentEntry ->
                    contentEntry.value.forEach { (team, enqueuedTasks) ->
                        item {
                            val isEmpty = remember(enqueuedTasks.size) {
                                enqueuedTasks.isEmpty()
                            }

                            if (!isEmpty) {
                                TeamSectionItem(
                                    content = contentEntry.key,
                                    team = team,
                                    enqueuedTasks = enqueuedTasks
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun TeamSectionItem(
    content: Content,
    team: String,
    enqueuedTasks: List<EnqueuedTask>,
    model: DownloadsViewModel = hiltViewModel()
) {
    val finishedTasks = remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    EnqueuedTeamItem(
        modifier = Modifier.padding(0.dp, 16.dp),
        title = content.name,
        tasksAmount = enqueuedTasks.size,
        team = team,
        finished = finishedTasks.intValue
    ) {
        enqueuedTasks.forEach { task ->
            val episodeFlow =
                model.episodesFlow(task.mediaDownloadTask.uid)
                    .collectAsStateWithLifecycle(null)
            val taskProgressFlow =
                task.progressState.collectAsStateWithLifecycle(0f)
            val taskStateFlow =
                task.state.collectAsStateWithLifecycle(TaskState.ENQUEUED)

            LaunchedEffect(taskStateFlow.value) {
                if (taskStateFlow.value == TaskState.FINISHED) {
                    finishedTasks.intValue++
                }
            }

            episodeFlow.value?.let { entity ->
                when (taskStateFlow.value) {
                    TaskState.STOPPED, TaskState.FINISHED, TaskState.PAUSED -> return@let
                    else -> {
                        EnqueuedTaskItem(
                            modifier = Modifier
                                .padding(16.dp, 0.dp)
                                .fillMaxWidth(),
                            episode = entity.episode,
                            name = entity.name
                                ?: stringResource(R.string.episode_string, entity.episode),
                            progress = taskProgressFlow.value,
                            buttonsEnabled = taskStateFlow.value != TaskState.CONVERTING,
                            onPause = {
                                model.pauseTask(task)
                            },
                            onCancel = {
                                coroutineScope.launch { task.state.emit(TaskState.STOPPED) }
                            }
                        )
                    }
                }
            }
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(16.dp, 4.dp).fillMaxWidth()
    )
}