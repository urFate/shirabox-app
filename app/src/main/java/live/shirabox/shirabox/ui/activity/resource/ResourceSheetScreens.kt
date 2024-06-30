package live.shirabox.shirabox.ui.activity.resource

import android.content.Context
import android.content.Intent
import android.text.format.DateUtils
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import live.shirabox.core.entity.EpisodeEntity
import live.shirabox.core.model.ActingTeam
import live.shirabox.core.model.Content
import live.shirabox.core.model.ContentType
import live.shirabox.core.util.IntentExtras
import live.shirabox.data.content.AbstractContentRepository
import live.shirabox.shirabox.R
import live.shirabox.shirabox.ui.activity.player.PlayerActivity
import live.shirabox.shirabox.ui.component.general.ExtendedListItem

@Composable
fun ResourceBottomSheet(
    content: Content,
    visibilityState: MutableState<Boolean>,
    model: ResourceViewModel = hiltViewModel()
) {
    val currentSheetScreenState = remember {
        mutableStateOf<ResourceSheetScreen>(ResourceSheetScreen.Sources(model))
    }

    val episodesState = model.fetchCachedEpisodes().collectAsState(initial = emptyList())

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

    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = {
            coroutineScope.launch {
                state.hide()
                visibilityState.value = false
            }
        }
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
                                        repository = repository,
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

    val skipPartiallyExpanded by remember { mutableStateOf(false) }
    val state = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = {
            coroutineScope.launch {
                state.hide()
                visibilityState.value = false

                currentSheetScreenState.value = ResourceSheetScreen.Sources(model)
            }
        }
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
                                ){
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
                        val isViewed = episodeEntity.watchingTime > 0
                        val textColor = if (isViewed)
                            Color.Gray else Color.Unspecified

                        ListItem(
                            overlineContent = {
                                Text(
                                    text = "#${episodeEntity.episode}",
                                    color = textColor
                                )
                            },
                            headlineContent = {
                                Text(
                                    text = if (episodeEntity.name.isNullOrEmpty())
                                        stringResource(
                                            R.string.anime_episode,
                                            episodeEntity.episode
                                        ) else episodeEntity.name.toString(),
                                    color = textColor
                                )
                            },
                            trailingContent = {
                                Text(updatedTimestamp.toString())
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
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamListItem(
    repository: AbstractContentRepository,
    content: Content,
    episodes: List<EpisodeEntity>,
    team: ActingTeam,
    model: ResourceViewModel = hiltViewModel(),
    onClick: () -> Unit
) {
    val context = LocalContext.current

    val isPinned = remember(model.pinnedTeams.size) {
        derivedStateOf { model.pinnedTeams.contains(team.name) }
    }
    val updatedTimestamp = remember {
        DateUtils.getRelativeTimeSpanString(
            context,
            episodes.first().uploadTimestamp
        )
    }

    ExtendedListItem(
        headlineContent = { Text(team.name) },
        supportingContent = {
            Text(
                pluralStringResource(id = R.plurals.episodes_plurals, count = episodes.size, episodes.size)
            )
        },
        overlineContent = { Text("Обновлено $updatedTimestamp") },
        coverImage = team.logoUrl,
        trailingIcon = if (isPinned.value) Icons.Filled.PushPin else Icons.Outlined.PushPin,
        headlineText = team.name,
        onTrailingIconClick = {
            model.switchTeamPinStatus(context, content.shikimoriID, repository, team)
        },
        onClick = onClick
    )
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