package com.shirabox.shirabox.ui.activity.resource

import android.content.Intent
import android.text.format.DateUtils
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shirabox.shirabox.R
import com.shirabox.shirabox.model.Content
import com.shirabox.shirabox.model.ContentType
import com.shirabox.shirabox.source.content.AbstractContentSource
import com.shirabox.shirabox.ui.activity.ReaderActivity
import com.shirabox.shirabox.ui.activity.player.PlayerActivity
import com.shirabox.shirabox.ui.component.general.ListItem
import kotlinx.coroutines.launch

@Composable
fun ResourceBottomSheet(
    content: Content,
    model: ResourceViewModel,
    visibilityState: MutableState<Boolean>
) {
    val currentSheetScreenState = remember {
        mutableStateOf<ResourceSheetScreen>(ResourceSheetScreen.Sources(model))
    }

    if(visibilityState.value) {
        when (currentSheetScreenState.value) {
            is ResourceSheetScreen.Sources -> SourcesSheetScreen(
                content = content,
                model = model,
                currentSheetScreenState = currentSheetScreenState,
                visibilityState = visibilityState
            )

            is ResourceSheetScreen.Episodes -> EpisodesSheetScreen(
                content = (currentSheetScreenState.value as ResourceSheetScreen.Episodes).content,
                source = (currentSheetScreenState.value as ResourceSheetScreen.Episodes).source,
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
    currentSheetScreenState: MutableState<ResourceSheetScreen>,
    visibilityState: MutableState<Boolean>,
    sources: List<AbstractContentSource> = model.sources
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
        val isReady by remember {
            derivedStateOf {
                model.episodesInfo.isNotEmpty()
            }
        }
        val isEmpty = remember(isReady) {
            if (isReady) model.episodesInfo.isEmpty() || model.episodesInfo.values.filterNotNull()
                .isEmpty() else false
        }

        LaunchedEffect(Unit) {
            sources.forEach { model.fetchEpisodesInfo(content.altName, it) }
        }

        Box (
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(rememberNestedScrollInteropConnection()),
            contentAlignment = Alignment.TopCenter
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = !isReady,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(64.dp)
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = isEmpty,
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
                visible = isReady,
                enter = fadeIn()
            ) {
                LazyColumn {
                    items(model.episodesInfo.size) {
                        val source = model.episodesInfo.keys.toList()[it]
                        val episodeInfo = model.episodesInfo.values.toList()[it]

                        episodeInfo?.let { episodesInfo ->
                            val updatedTimestamp = DateUtils.getRelativeTimeSpanString(
                                LocalContext.current,
                                episodesInfo.lastEpisodeTimestamp * 1000L
                            )

                            ListItem(
                                headlineContent = { Text(source.name) },
                                supportingContent = { Text("${episodesInfo.episodes} " +
                                        if (content.type == ContentType.ANIME) "Серий" else "Глав"
                                ) },
                                overlineContent = { Text("Обновлено $updatedTimestamp") },
                                coverImage = source.icon,
                                trailingIcon = Icons.Outlined.PushPin,
                                onTrailingIconClick = { /*TODO*/ }
                            ) {
                                currentSheetScreenState.value = ResourceSheetScreen.Episodes(content, source)
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
    source: AbstractContentSource,
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
    val episodes by remember {
        derivedStateOf {
            model.episodes[source]
        }
    }

    val isReady by remember {
        derivedStateOf {
            !episodes.isNullOrEmpty()
        }
    }
    val isEmpty by remember {
        derivedStateOf {
            episodes?.isEmpty() ?: false
        }
    }

    LaunchedEffect(Unit) {
        model.fetchEpisodes(content.altName, source)
    }

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
                visible = !isReady,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(64.dp)
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = isEmpty,
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
                visible = isReady,
                enter = fadeIn()
            ) {
                LazyColumn {
                    episodes?.let { episodes ->
                        items(episodes) {
                            val updatedTimestamp =
                                DateUtils.getRelativeTimeSpanString(LocalContext.current, it.uploadTimestamp * 1000L)

                            ListItem(
                                headlineString = if(it.name.isNullOrEmpty()) "Эпизод #${it.episode}" else it.name,
                                trailingString = updatedTimestamp.toString(),
                                overlineString = "#${it.episode}"
                            ) {
                                when(it.type) {
                                    ContentType.ANIME -> context.startActivity(Intent(context, PlayerActivity::class.java))
                                    else -> context.startActivity(Intent(context, ReaderActivity::class.java))
                                }
                            }
                        }
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