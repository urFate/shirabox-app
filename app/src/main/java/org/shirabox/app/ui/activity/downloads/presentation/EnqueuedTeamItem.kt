package org.shirabox.app.ui.activity.downloads.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import org.shirabox.app.ui.activity.downloads.DownloadsViewModel

@Composable
fun EnqueuedTeamItem(
    modifier: Modifier = Modifier,
    model: DownloadsViewModel = hiltViewModel(),
    title: String,
    team: String,
    tasksAmount: Int,
    finished: Int,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val finishedTasks = remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(16.dp, 0.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column (
                modifier = Modifier.weight(weight = 1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = team,
                    fontSize = 12.sp
                )
            }

            Row(
                modifier = Modifier.padding(16.dp, 0.dp)
            ) {
                Text(
                    text = "${tasksAmount}/${finished}",
                    fontSize = 14.sp
                )
            }
        }

        content()

        /**
         * tasks.forEach { task ->
         *             val episodeFlow = model.episodesFlow(task.mediaDownloadTask.uid).collectAsStateWithLifecycle(null)
         *             val taskProgressFlow = task.progressState.collectAsStateWithLifecycle(0f)
         *             val taskStateFlow = task.state.collectAsStateWithLifecycle(TaskState.ENQUEUED)
         *
         *             LaunchedEffect(taskStateFlow.value) {
         *                 if (taskStateFlow.value == TaskState.FINISHED) {
         *                     finishedTasks.intValue++;
         *                 }
         *             }
         *
         *             episodeFlow.value?.let { episode ->
         *                 when (taskStateFlow.value) {
         *                     TaskState.STOPPED, TaskState.FINISHED, TaskState.PAUSED -> return@let
         *                     else -> {
         *                         EnqueuedTaskItem(
         *                             modifier = Modifier
         *                                 .padding(16.dp, 0.dp)
         *                                 .fillMaxWidth(),
         *                             episode = episode.episode,
         *                             name = episode.name ?: "Серия #${episode.episode}",
         *                             progress = taskProgressFlow.value,
         *                             state = taskStateFlow.value,
         *                             onPause = {
         *                                 coroutineScope.launch { task.state.emit(TaskState.PAUSED) }
         *                             },
         *                             onCancel = {
         *                                 coroutineScope.launch { task.state.emit(TaskState.STOPPED) }
         *                             }
         *                         )
         *                     }
         *                 }
         *             }
         *         }
         */
    }
}

@Preview
@Composable
fun Prew() {
    Row(
        modifier = Modifier.padding(16.dp, 0.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column (
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "title",
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "team",
                fontSize = 12.sp
            )
        }

        Row {
            Text("0/1")
        }
    }
}
