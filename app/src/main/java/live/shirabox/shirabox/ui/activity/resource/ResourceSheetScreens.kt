package live.shirabox.shirabox.ui.activity.resource

import android.content.Intent
import android.text.format.DateUtils
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import live.shirabox.core.entity.EpisodeEntity
import live.shirabox.core.model.Content
import live.shirabox.core.model.ContentType
import live.shirabox.data.content.AbstractContentSource
import live.shirabox.shirabox.R
import live.shirabox.shirabox.ui.activity.player.PlayerActivity
import live.shirabox.shirabox.ui.component.general.ExtendedListItem

@Composable
fun ResourceBottomSheet(
    content: Content,
    model: ResourceViewModel,
    visibilityState: MutableState<Boolean>
) {
    val currentSheetScreenState = remember {
        mutableStateOf<ResourceSheetScreen>(ResourceSheetScreen.Sources(model))
    }

    val episodesState = model.fetchCachedEpisodes().collectAsState(initial = emptyList())

    val episodes = remember(episodesState.value, model.databaseUid) {
        model.databaseUid.intValue.let { uid ->
            if (uid >= 0) episodesState.value.filter { it.contentUid == model.databaseUid.intValue }
            else emptyList()
        }
    }

    val sortedEpisodesMap: Map<AbstractContentSource?, List<EpisodeEntity>> = remember(episodes) {
        episodes.groupBy { it.source }
            .mapKeys { map ->
                model.sources.find { it.name == map.key }
            }
            .mapValues { entry ->
                entry.value.sortedByDescending { it.episode }
            }
    }

    LaunchedEffect(Unit) {
        // Update cache
        model.fetchEpisodes(content)
    }

    if (visibilityState.value) {
        when (currentSheetScreenState.value) {
            is ResourceSheetScreen.Sources -> SourcesSheetScreen(
                content = content,
                model = model,
                episodes = sortedEpisodesMap,
                currentSheetScreenState = currentSheetScreenState,
                visibilityState = visibilityState
            )

            is ResourceSheetScreen.Episodes -> EpisodesSheetScreen(
                content = (currentSheetScreenState.value as ResourceSheetScreen.Episodes).content,
                episodes = sortedEpisodesMap[(currentSheetScreenState.value as ResourceSheetScreen.Episodes).source]
                    ?: emptyList(),
                model = model,
                currentSheetScreenState = currentSheetScreenState,
                visibilityState = visibilityState
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourcesSheetScreen(
    content: Content,
    model: ResourceViewModel,
    episodes: Map<AbstractContentSource?, List<EpisodeEntity>>,
    currentSheetScreenState: MutableState<ResourceSheetScreen>,
    visibilityState: MutableState<Boolean>,
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
                    modifier = Modifier.padding(64.dp)
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
                        val source = data.key
                        val entityList = data.value

                        source?.let {
                            item {
                                val context = LocalContext.current

                                val isPinned by remember(model.pinnedSources) {
                                    derivedStateOf { model.pinnedSources.contains(source.name) }
                                }
                                val updatedTimestamp = remember {
                                    DateUtils.getRelativeTimeSpanString(
                                        context,
                                        entityList.first().uploadTimestamp * 1000L
                                    )
                                }

                                ExtendedListItem(
                                    headlineContent = { Text(source.name) },
                                    supportingContent = {
                                        Text(
                                            "${entityList.size} " +
                                                    if (content.type == ContentType.ANIME) "Серий" else "Глав"
                                        )
                                    },
                                    overlineContent = { Text("Обновлено $updatedTimestamp") },
                                    coverImage = source.icon,
                                    trailingIcon = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                    onTrailingIconClick = {
                                        model.switchSourcePinStatus(content.shikimoriID, source)
                                    }
                                ) {
                                    currentSheetScreenState.value =
                                        ResourceSheetScreen.Episodes(content, source)
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
    model: ResourceViewModel,
    currentSheetScreenState: MutableState<ResourceSheetScreen>,
    visibilityState: MutableState<Boolean>
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
                    modifier = Modifier.padding(64.dp)
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
                    items(episodes) { episodeEntity ->
                        val updatedTimestamp =
                            DateUtils.getRelativeTimeSpanString(
                                LocalContext.current,
                                episodeEntity.uploadTimestamp * 1000L
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
                                    ContentType.ANIME -> context.startActivity(
                                        Intent(
                                            context,
                                            PlayerActivity::class.java
                                        ).apply {
                                            val playlist = episodes.map {
                                                live.shirabox.core.model.PlaylistVideo(
                                                    episode = it.episode,
                                                    streamUrls = it.videos,
                                                    openingMarkers = it.videoMarkers
                                                )
                                            }.reversed()

                                            putExtra("content_uid", episodeEntity.contentUid)
                                            putExtra("name", content.name)
                                            putExtra("episode", episodeEntity.episode)
                                            putExtra("start_index", playlist.indexOfFirst { it.episode == episodeEntity.episode})
                                            putExtra("playlist", Json.encodeToString(playlist))
                                        })

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

sealed class ResourceSheetScreen {
    data class Sources(val model: ResourceViewModel) : ResourceSheetScreen()
    data class Episodes(val content: Content, val source: AbstractContentSource) : ResourceSheetScreen()
}