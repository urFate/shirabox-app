package org.shirabox.app.ui.activity.downloads.screen

import android.content.Intent
import android.text.format.DateUtils
import android.text.format.Formatter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Hd
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.Sd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.combine
import org.shirabox.app.R
import org.shirabox.app.ui.activity.downloads.DownloadsViewModel
import org.shirabox.app.ui.activity.player.PlayerActivity
import org.shirabox.app.ui.activity.resource.ResourceActivity
import org.shirabox.core.entity.EpisodeEntity
import org.shirabox.core.model.ContentType
import org.shirabox.core.model.Quality
import org.shirabox.core.util.IntentExtras
import org.shirabox.core.util.Util
import kotlin.collections.component1
import kotlin.collections.component2

@Composable
fun DownloadsSavedScreen(model: DownloadsViewModel = hiltViewModel()) {
    val offlineEpisodesState = model.offlineEpisodesFlow()
        .combine(model.offlineFlowFilter) { entry, s -> entry.filterKeys { it.name.contains(s, true) } }
        .collectAsStateWithLifecycle(emptyMap())
    val removalDialogState = remember { mutableStateOf(false) }
    val removalDialogEpisodes = remember { mutableStateListOf<EpisodeEntity>() }
    val multipleRemoval = remember(removalDialogEpisodes.size) {
        removalDialogEpisodes.size > 1
    }
    val context = LocalContext.current
    val listState = rememberLazyListState()

    Scaffold(
        floatingActionButton = {
            AnimatedVisibility(
                visible = offlineEpisodesState.value.isNotEmpty()
            ) {
                ExtendedFloatingActionButton(
                    icon = { Icon(Icons.Filled.DeleteSweep, "Delete all") },
                    text = { Text(text = stringResource(R.string.downloads_delete_all)) },
                    expanded = !listState.canScrollBackward,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    onClick = {
                        removalDialogEpisodes.clear()
                        removalDialogEpisodes.addAll(offlineEpisodesState.value.flatMap {
                            it.value.values.flatten()
                        })
                        removalDialogState.value = true
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
                visible = offlineEpisodesState.value.isEmpty(),
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
                        painter = painterResource(R.drawable.ic_self_improvement),
                        tint = MaterialTheme.colorScheme.outline,
                        contentDescription = "Fresh"
                    )
                    Text(
                        text = stringResource(R.string.downloads_empty_library),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.padding(0.dp, 8.dp),
                state = listState
            ) {
                offlineEpisodesState.value.entries.forEach { contentEntry ->
                    contentEntry.value.forEach { (team, episodes) ->
                        item {
                            val isEmpty = remember(episodes.size) {
                                episodes.isEmpty()
                            }
                            val teamFilesSize = model.calculateEpisodesSize(
                                episodes
                                    .filter { it.offlineVideos != null }
                                    .map { it.offlineVideos!!.values }.flatten()
                            ).collectAsStateWithLifecycle(0L)

                            val teamFilesSizeText = remember(teamFilesSize.value) {
                                Formatter.formatShortFileSize(context, teamFilesSize.value)
                            }

                            if (!isEmpty) {
                                Column(
                                    modifier = Modifier
                                        .clickable {
                                            context.startActivity(
                                                Intent(
                                                    context,
                                                    ResourceActivity::class.java
                                                ).apply {
                                                    putExtra("id", contentEntry.key.shikimoriID)
                                                    putExtra("type", ContentType.ANIME)
                                                }
                                            )
                                        }
                                        .padding(16.dp, 8.dp)
                                        .fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            modifier = Modifier.weight(weight = 1f, fill = false),
                                            text = contentEntry.key.name,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "($team)",
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = "${pluralStringResource(R.plurals.episodes_plurals, episodes.size, episodes.size)} ($teamFilesSizeText)",
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        items(episodes) { episode ->
                            val uploadTimestamp = remember {
                                DateUtils.getRelativeTimeSpanString(
                                    context,
                                    episode.uploadTimestamp
                                )
                            }

                            val qualityVector = remember {
                                when(episode.offlineVideos?.keys?.firstOrNull()) {
                                    Quality.SD -> Icons.Rounded.Sd
                                    Quality.HD -> Icons.Rounded.Hd
                                    Quality.FHD -> Icons.Rounded.HighQuality
                                    null -> Icons.Rounded.HighQuality
                                }
                            }

                            ListItem(
                                modifier = Modifier.clickable {
                                    context.startActivity(Intent(context, PlayerActivity::class.java).apply {
                                        putExtras(IntentExtras.playerIntentExtras(
                                            content = Util.mapEntityToContent(contentEntry.key),
                                            episodeEntity = episode,
                                            team = team
                                        ))
                                    })
                                },
                                overlineContent = {
                                    Text(
                                        text = "#${episode.episode} (${uploadTimestamp})",
                                        fontSize = 12.sp
                                    )
                                },
                                headlineContent = {
                                    val episodeName = if (episode.name.isNullOrEmpty()) stringResource(R.string.episode_string, episode.episode) else episode.name

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            modifier = Modifier.weight(weight = 1f, fill = false),
                                            text = episodeName.toString(),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Icon(
                                            modifier = Modifier.size(21.dp),
                                            imageVector = qualityVector,
                                            tint = MaterialTheme.colorScheme.primary,
                                            contentDescription = null
                                        )
                                    }
                                },
                                trailingContent = {
                                    val episodeFileSize = model.calculateEpisodesSize(
                                        listOf(episode.offlineVideos!!.values.first())
                                    ).collectAsStateWithLifecycle(0L)

                                    val episodeFileSizeText = remember(episodeFileSize.value) {
                                        Formatter.formatShortFileSize(context, episodeFileSize.value)
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = episodeFileSizeText,
                                            fontSize = 12.sp
                                        )

                                        IconButton(
                                            onClick = {
                                                removalDialogEpisodes.clear()
                                                removalDialogEpisodes.add(episode)
                                                removalDialogState.value = true
                                            }
                                        ) {
                                            Icon(
                                                modifier = Modifier.size(24.dp),
                                                imageVector = Icons.Rounded.DeleteOutline,
                                                tint = MaterialTheme.colorScheme.error,
                                                contentDescription = "delete"
                                            )
                                        }
                                    }
                                }
                            )
                        }

                        item {
                            HorizontalDivider(modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp))
                        }
                    }
                }
            }
        }
    }

    RemovalConfirmDialog(dialogVisibilityState = removalDialogState, multipleRemoval) {
        model.deleteOfflineEpisodes(*removalDialogEpisodes.toTypedArray())
    }
}

@Composable
internal fun RemovalConfirmDialog(
    dialogVisibilityState: MutableState<Boolean>,
    multipleRemoval: Boolean,
    onConfirm: () -> Unit
) {
    if (dialogVisibilityState.value) {
        val text = remember(multipleRemoval) {
            if (multipleRemoval) "Удалить все сохранённые серии?" else "Удалить сохранённую серию?"
        }

        AlertDialog(
            icon = {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = Icons.Filled.DeleteSweep,
                    contentDescription = "Delete sweep"
                )
            },
            title = {
                Text(text = "Погоди-ка...")
            },
            text = {
                Text(text = text)
            },
            onDismissRequest = {
                dialogVisibilityState.value = false
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm()
                        dialogVisibilityState.value = false
                    },
                    colors = ButtonDefaults.buttonColors()
                        .copy(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton (
                    onClick = {
                        dialogVisibilityState.value = false
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}