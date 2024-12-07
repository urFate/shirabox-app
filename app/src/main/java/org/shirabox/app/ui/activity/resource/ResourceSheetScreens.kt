package org.shirabox.app.ui.activity.resource

import android.content.Context
import android.content.Intent
import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.OfflinePin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material.icons.rounded.Hd
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Sd
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastAny
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.shirabox.app.ComposeUtils.bottomSheetDynamicNavColor
import org.shirabox.app.R
import org.shirabox.app.service.media.DownloadsServiceHelper
import org.shirabox.app.service.media.model.TaskState
import org.shirabox.app.ui.activity.downloads.DownloadsActivity
import org.shirabox.app.ui.activity.player.PlayerActivity
import org.shirabox.app.ui.component.general.ExtendedListItem
import org.shirabox.app.ui.component.general.QualityDialog
import org.shirabox.core.entity.EpisodeEntity
import org.shirabox.core.model.ActingTeam
import org.shirabox.core.model.Content
import org.shirabox.core.model.ContentKind
import org.shirabox.core.model.ContentType
import org.shirabox.core.model.Quality
import org.shirabox.core.util.IntentExtras
import org.shirabox.data.content.AbstractContentRepository
import kotlin.math.roundToInt

@Composable
fun ResourceBottomSheet(
    content: Content,
    visibilityState: MutableState<Boolean>,
    model: ResourceViewModel = hiltViewModel()
) {
    val currentSheetScreenState = remember {
        mutableStateOf<ResourceSheetScreen>(ResourceSheetScreen.Sources(model))
    }

    val episodesState = model.cachedEpisodesFlow().collectAsState(initial = emptyList())

    val episodes = remember(episodesState.value, model.internalContentUid) {
        model.internalContentUid.longValue.let { uid ->
            if (uid >= 0) episodesState.value.filter { it.contentUid == model.internalContentUid.longValue }
            else emptyList()
        }
    }

    val sortedEpisodesMap: Map<AbstractContentRepository?, Map<String, List<EpisodeEntity>>> = remember(episodes) {
        episodes.groupBy { it.source }
            .mapKeys { map ->
                model.repositories.find { it.name == map.key }
            }
            .mapValues { entry ->
                entry.value.sortedByDescending { it.episode }
            }.map { entry ->
                entry.key to entry.value.groupBy { it.actingTeamName }
            }.toMap()
    }

    LaunchedEffect(Unit) {
        // Update cache
        model.fetchEpisodes(content)
    }

    if (visibilityState.value) {
        when (currentSheetScreenState.value) {
            is ResourceSheetScreen.Sources -> SourcesSheetScreen(
                content = content,
                episodes = sortedEpisodesMap,
                currentSheetScreenState = currentSheetScreenState,
                visibilityState = visibilityState
            )

            is ResourceSheetScreen.Episodes -> {
                val instance = currentSheetScreenState.value as ResourceSheetScreen.Episodes

                EpisodesSheetScreen(
                    content = (currentSheetScreenState.value as ResourceSheetScreen.Episodes).content,
                    episodes = sortedEpisodesMap[instance.repository]?.get(instance.team.name)
                        ?: emptyList(),
                    team = instance.team,
                    currentSheetScreenState = currentSheetScreenState,
                    visibilityState = visibilityState
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourcesSheetScreen(
    content: Content,
    episodes: Map<AbstractContentRepository?, Map<String, List<EpisodeEntity>>>,
    currentSheetScreenState: MutableState<ResourceSheetScreen>,
    visibilityState: MutableState<Boolean>,
    model: ResourceViewModel = hiltViewModel()
) {
    val skipPartiallyExpanded by remember { mutableStateOf(false) }
    val state = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val coroutineScope = rememberCoroutineScope()

    bottomSheetDynamicNavColor(state)

    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = {
            coroutineScope.launch {
                state.hide()
                visibilityState.value = false
            }
        },
        contentWindowInsets = { BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Bottom) },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(rememberNestedScrollInteropConnection()),
            contentAlignment = Alignment.TopCenter
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = episodes.isEmpty() && !model.episodeFetchComplete.value,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(64.dp),
                    strokeCap = StrokeCap.Round
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = model.episodeFetchComplete.value && episodes.isEmpty(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(64.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "(￢_￢;)",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(id = R.string.no_sources),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraLight,
                        textAlign = TextAlign.Center
                    )
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = episodes.isNotEmpty(),
                enter = fadeIn()
            ) {
                LazyColumn {
                    episodes.forEach { data ->
                        val repository = data.key
                        val actingTeams = data.value.entries.sortedByDescending {
                            model.pinnedTeams.contains(it.key)
                        }

                        repository?.let {
                            if(actingTeams.isEmpty()) return@let

                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = repository.name,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onBackground.copy(0.7f)
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.height(2.dp)
                                    )
                                }
                            }

                            actingTeams.forEach { (team, entities) ->
                                item {
                                    val actingTeam = ActingTeam(team, entities.first().actingTeamIcon)

                                    TeamListItem(
                                        content = content,
                                        episodes = entities,
                                        team = actingTeam
                                    ) {
                                        currentSheetScreenState.value =
                                            ResourceSheetScreen.Episodes(content, repository, actingTeam)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodesSheetScreen(
    content: Content,
    episodes: List<EpisodeEntity>,
    team: ActingTeam,
    currentSheetScreenState: MutableState<ResourceSheetScreen>,
    visibilityState: MutableState<Boolean>,
    model: ResourceViewModel = hiltViewModel()
    ) {
    val context = LocalContext.current
    val downloadsHelper = DownloadsServiceHelper

    val skipPartiallyExpanded by remember { mutableStateOf(false) }
    val state = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val coroutineScope = rememberCoroutineScope()
    val pausedTasks = model.pausedTasksFlow().collectAsStateWithLifecycle(emptyList())

    bottomSheetDynamicNavColor(state)

    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = {
            coroutineScope.launch {
                state.hide()
                visibilityState.value = false

                currentSheetScreenState.value = ResourceSheetScreen.Sources(model)
            }
        },
        contentWindowInsets = { BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Bottom) }
        ) {
        Box (
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(rememberNestedScrollInteropConnection()),
            contentAlignment = Alignment.TopCenter
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = episodes.isEmpty() && !model.episodeFetchComplete.value,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(64.dp),
                    strokeCap = StrokeCap.Round
                )
            }

            /*
                Show emoticon when something went wrong
                (e.g. unstable internet connection or service unavailability)
             */

            androidx.compose.animation.AnimatedVisibility(
                visible = model.episodeFetchComplete.value && episodes.isEmpty(),
                enter = fadeIn()
            ) {
                Column(
                    modifier = Modifier.padding(64.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "(；⌣̀_⌣́)",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(id = R.string.no_contents),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraLight,
                        textAlign = TextAlign.Center
                    )
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = episodes.isNotEmpty(),
                enter = fadeIn()
            ) {
                LazyColumn {
                    val lastViewedEpisode = episodes.firstOrNull { it.watchingTime != -1L }
                        ?: episodes.reversed().first()

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp, 0.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier.widthIn(
                                    0.dp,
                                    (LocalConfiguration.current.screenWidthDp / 3).dp
                                ),
                                text = team.name,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground.copy(0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            HorizontalDivider(
                                modifier = Modifier
                                    .height(2.dp)
                                    .fillMaxWidth()
                                    .weight(weight = 1f, fill = false)
                            )
                            Button(
                                onClick = {
                                    startPlayerActivity(
                                        context = context,
                                        content = content,
                                        episodeEntity = lastViewedEpisode,
                                        team = team
                                    )
                                }
                            ) {
                                Row (
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = "Play")
                                    Text(
                                        text = when(lastViewedEpisode.watchingTime) {
                                            -1L -> stringResource(id = R.string.watch)
                                            else -> stringResource(id = R.string.continue_watching)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    items(episodes) { episodeEntity ->
                        val updatedTimestamp =
                            DateUtils.getRelativeTimeSpanString(
                                LocalContext.current,
                                episodeEntity.uploadTimestamp
                            )
                        val headlineText = if (episodeEntity.name.isNullOrEmpty()) {
                            if (content.kind == ContentKind.MOVIE) {
                                stringResource(id = R.string.kind_movie)
                            } else stringResource(R.string.anime_episode, episodeEntity.episode)
                        } else episodeEntity.name.toString()

                        val isViewed = episodeEntity.watchingTime > 0
                        val isOffline = episodeEntity.offlineVideos?.isNotEmpty() == true
                        val textColor = if (isViewed)
                            Color.Gray else Color.Unspecified

                        val maxQuality = remember { episodeEntity.videos.keys.max() }

                        val enqueuedDownloadingTask =
                            downloadsHelper.getEnqueuedTask(model.internalContentUid.longValue, episodeEntity.uid)
                                .collectAsStateWithLifecycle(null)
                        val taskState =
                            enqueuedDownloadingTask.value?.state?.collectAsStateWithLifecycle()
                        val downloadProgress =
                            enqueuedDownloadingTask.value?.progressState?.collectAsStateWithLifecycle()

                        val isTaskEnqueued = remember(taskState?.value) {
                            taskState?.value == TaskState.ENQUEUED || taskState?.value == TaskState.IN_PROGRESS
                        }

                        val pausedTask = remember(pausedTasks.value.size) {
                            pausedTasks.value.firstOrNull { it.episodeUid == episodeEntity.uid }
                        }

                        ListItem(
                            overlineContent = {
                                Text(
                                    text = "#${episodeEntity.episode} ($updatedTimestamp)",
                                    color = textColor
                                )
                            },
                            headlineContent = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = headlineText,
                                        color = textColor
                                    )

                                    if (isOffline) {
                                        val qualityVector = when(episodeEntity.offlineVideos?.keys?.firstOrNull()) {
                                            Quality.SD -> Icons.Rounded.Sd
                                            Quality.HD -> Icons.Rounded.Hd
                                            Quality.FHD -> Icons.Rounded.HighQuality
                                            null -> Icons.Rounded.HighQuality
                                        }

                                        Icon(
                                            modifier = Modifier.size(21.dp),
                                            imageVector = qualityVector,
                                            tint = MaterialTheme.colorScheme.primary,
                                            contentDescription = null
                                        )
                                    }
                                }
                            },
                            trailingContent = {
                                val qualityDialogVisible = remember { mutableStateOf(false) }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val percentProgress = remember(downloadProgress?.value) {
                                        downloadProgress?.value?.times(100)?.roundToInt() ?: 0
                                    }

                                    AnimatedVisibility(visible = percentProgress > 0, enter = fadeIn(), exit = fadeOut()) {
                                        Text("$percentProgress%")
                                    }

                                    AnimatedVisibility(visible = isOffline, enter = fadeIn(), exit = fadeOut()) {
                                        IconButton(
                                            onClick = {
                                                model.deleteOfflineEpisodes(episodeEntity)
                                            }
                                        ) {
                                            Icon(
                                                modifier = Modifier.size(24.dp),
                                                imageVector = Icons.Rounded.DeleteOutline,
                                                contentDescription = "download"
                                            )
                                        }
                                    }

                                    AnimatedVisibility(visible = pausedTask != null, enter = fadeIn(), exit = fadeOut()) {
                                        PausedTaskButton(pausedTask?.pausedProgress ?: 0f)
                                    }

                                    AnimatedVisibility(visible = !isOffline, enter = fadeIn(), exit = fadeOut()) {
                                        DownloadButton(
                                            isDownloading = isTaskEnqueued,
                                            isAnyEpisodeOffline = false,
                                            progress = downloadProgress?.value ?: 0.0f
                                        ) {
                                            if (!isTaskEnqueued) {
                                                qualityDialogVisible.value = true
                                            } else {
                                                coroutineScope.launch(Dispatchers.IO) {
                                                    enqueuedDownloadingTask.value?.state?.emit(TaskState.STOPPED)
                                                }
                                            }
                                        }
                                    }
                                }

                                QualityDialog(
                                    title = stringResource(R.string.offline_quality_dialog_title),
                                    description = stringResource(R.string.offline_quality_dialog_desc),
                                    icon = Icons.Outlined.OfflinePin,
                                    visibilityState = qualityDialogVisible,
                                    maxQuality = maxQuality,
                                    autoSelect = maxQuality
                                ) {
                                    model.saveEpisodes(context = context, quality = it, episodeEntity)
                                }
                            },
                            modifier = Modifier.clickable {
                                when (episodeEntity.type) {
                                    ContentType.ANIME -> startPlayerActivity(
                                        context = context,
                                        content = content,
                                        episodeEntity = episodeEntity,
                                        team = team
                                    )

                                    else -> {}
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
            }
        }
    }


}

@Composable
private fun TeamListItem(
    content: Content,
    episodes: List<EpisodeEntity>,
    team: ActingTeam,
    model: ResourceViewModel = hiltViewModel(),
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val contentUid = model.internalContentUid.longValue
    val downloadsHelper = DownloadsServiceHelper

    val isPinned = remember(model.pinnedTeams.size) {
        derivedStateOf { model.pinnedTeams.contains(team.name) }
    }

    val updatedTimestamp = remember {
        DateUtils.getRelativeTimeSpanString(
            context,
            episodes.first().uploadTimestamp
        )
    }

    var downloadingProgress by remember { mutableFloatStateOf(0f) }
    var isGroupEnqueued by remember { mutableStateOf(false) }

    val isAnyEpisodeOffline = remember(episodes) { episodes.fastAny { !it.offlineVideos.isNullOrEmpty() }}
    val isAllEpisodesOffline = remember(episodes) { episodes.all { !it.offlineVideos.isNullOrEmpty() } }

    val enqueuedDownloadingTasks by
        downloadsHelper.getQueryByGroupId(contentUid, team.name).collectAsStateWithLifecycle(emptyList())

    LaunchedEffect(enqueuedDownloadingTasks) {
        enqueuedDownloadingTasks?.forEach { enqueuedTask ->
            enqueuedTask.state.collect {
                isGroupEnqueued = (it == TaskState.ENQUEUED) || (it == TaskState.IN_PROGRESS)
            }
        }
    }

    LaunchedEffect(enqueuedDownloadingTasks) {
        if (isGroupEnqueued) {
            val summaryProgress = mutableMapOf<Int, Float>()

            enqueuedDownloadingTasks?.forEach { enqueuedTask ->
                launch {
                    enqueuedTask.progressState.collect {
                        summaryProgress[enqueuedTask.mediaDownloadTask.uid] = it

                        val tasksAmount = enqueuedDownloadingTasks?.size ?: 0
                        downloadingProgress = summaryProgress.values.sum().div(tasksAmount)
                    }
                }
            }
        } else {
            downloadingProgress = 0f
        }
    }

    ExtendedListItem(
        headlineContent = { Text(team.name) },
        supportingContent = {
            if (content.kind != ContentKind.MOVIE) {
                Text(
                    pluralStringResource(id = R.plurals.episodes_plurals, count = episodes.size, episodes.size)
                )
            }
        },
        overlineContent = { Text("Обновлено $updatedTimestamp") },
        coverImage = team.logoUrl,
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy((-8).dp)
            ) {
                val qualityDialogVisible = remember { mutableStateOf(false) }

                if (isAllEpisodesOffline) {
                    IconButton(
                        onClick = {
                            model.deleteOfflineEpisodes(*episodes.toTypedArray())
                        }
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "download"
                        )
                    }
                } else {
                    DownloadButton(
                        isDownloading = isGroupEnqueued,
                        isAnyEpisodeOffline = isAnyEpisodeOffline,
                        progress = downloadingProgress
                    ) {
                        if(!isGroupEnqueued) {
                            qualityDialogVisible.value = true
                        } else {
                            coroutineScope.launch(Dispatchers.IO) {
                                downloadsHelper.stopByGroupId(model.internalContentUid.longValue, team.name)
                            }
                        }
                    }
                }

                IconButton(onClick = { model.switchTeamPinStatus(content.shikimoriId, team) }) {
                    Icon(
                        imageVector = if (isPinned.value) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = "Trailing Icon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val maxQuality = remember(episodes.size) {
                    Quality.valueOfInt(
                        episodes.maxOf { entity -> entity.videos.keys.maxOf { it.quality } }
                    )
                }

                QualityDialog(
                    title = stringResource(R.string.offline_quality_dialog_title),
                    description = stringResource(R.string.offline_quality_dialog_desc),
                    icon = Icons.Outlined.OfflinePin,
                    visibilityState = qualityDialogVisible,
                    maxQuality = maxQuality,
                    autoSelect = maxQuality
                ) {
                    model.saveEpisodes(context = context, it, *episodes.reversed().toTypedArray())
                }
            }
        },
        headlineText = team.name,
        onTrailingIconClick = {},
        onClick = onClick
    )
}

@Composable
private fun DownloadButton(
    isAnyEpisodeOffline: Boolean,
    isDownloading: Boolean,
    progress: Float,
    onClick: () -> Unit
) {
    val iconSize = remember(isDownloading) {
        if (!isDownloading) 24.dp else 16.dp
    }

    IconButton(
        onClick = { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = isDownloading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (progress > 0.001f) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        progress = { progress },
                        trackColor = ProgressIndicatorDefaults.circularDeterminateTrackColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                        strokeWidth = 2.dp
                    )
                }
            }

            val vector = if (isDownloading) {
                Icons.Rounded.Stop
            } else if (isAnyEpisodeOffline) {
                Icons.Rounded.Downloading
            } else {
                ImageVector.vectorResource(id = R.drawable.download_for_offline)
            }

            Icon(
                modifier = Modifier
                    .animateContentSize()
                    .size(iconSize),
                imageVector = vector,
                contentDescription = "download"
            )
        }
    }
}

@Composable
fun PausedTaskButton(pausedProgress: Float) {
    val context = LocalContext.current

    IconButton(
        onClick = {
            context.startActivity(
                Intent(
                    context,
                    DownloadsActivity::class.java
                ).apply { putExtra("tab", 1) })
        }
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                progress = { pausedProgress },
                trackColor = ProgressIndicatorDefaults.circularDeterminateTrackColor,
                strokeWidth = 2.dp
            )

            Icon(
                modifier = Modifier
                    .animateContentSize()
                    .size(16.dp),
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = "resume"
            )
        }
    }
}

private fun startPlayerActivity(
    context: Context,
    content: Content,
    episodeEntity: EpisodeEntity,
    team: ActingTeam
) {
    context.startActivity(Intent(context, PlayerActivity::class.java).apply {
        putExtras(IntentExtras.playerIntentExtras(
            content = content,
            episodeEntity = episodeEntity,
            team = team.name
        ))
    })
}

sealed class ResourceSheetScreen {
    data class Sources(val model: ResourceViewModel) : ResourceSheetScreen()
    data class Episodes(val content: Content, val repository: AbstractContentRepository, val team: ActingTeam) : ResourceSheetScreen()
}