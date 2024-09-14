package org.shirabox.app.ui.activity.settings.category.playback

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FastForward
import androidx.compose.material.icons.outlined.Hd
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Sd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.shirabox.app.R
import org.shirabox.core.datastore.AppDataStore
import org.shirabox.core.datastore.DataStoreScheme
import kotlin.math.roundToInt

@Composable
fun QualityDialog(visibilityState: MutableState<Boolean>) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val field = DataStoreScheme.FIELD_DEFAULT_QUALITY

    val currentQualityFlowState =
        AppDataStore.read(context, field.key).collectAsState(
            initial = field.defaultValue
        )
    val currentQuality = remember(currentQualityFlowState.value) {
        currentQualityFlowState.value ?: field.defaultValue
    }

    if (visibilityState.value) {
        AlertDialog(
            onDismissRequest = {
                visibilityState.value = false
            },
            confirmButton = {
                TextButton(onClick = { visibilityState.value = false }) {
                    Text(text = stringResource(id = R.string.confirm))
                }
            },
            icon = {
                Icon(
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary,
                    imageVector = Icons.Outlined.HighQuality,
                    contentDescription = "quality"
                )
            },
            title = { Text(stringResource(id = R.string.playback_default_quality)) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = stringResource(id = R.string.playback_default_quality_desc))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        content = {
                            QualityListItem(
                                text = stringResource(id = R.string.high_quality),
                                description = "1080p",
                                icon = Icons.Outlined.HighQuality,
                                selected = (currentQuality == 1080)
                            ) {
                                coroutineScope.launch {
                                    AppDataStore.write(context, field.key, 1080)
                                }
                            }

                            QualityListItem(
                                text = stringResource(id = R.string.medium_quality),
                                description = "720p",
                                icon = Icons.Outlined.Hd,
                                selected = (currentQuality == 720)
                            ) {
                                coroutineScope.launch {
                                    AppDataStore.write(context, field.key, 720)
                                }
                            }

                            QualityListItem(
                                text = stringResource(id = R.string.low_quality),
                                description = "480p",
                                icon = Icons.Outlined.Sd,
                                selected = (currentQuality == 480)
                            ) {
                                coroutineScope.launch {
                                    AppDataStore.write(context, field.key, 480)
                                }
                            }
                        }
                    )
                }
            }
        )
    }
}

@Composable
private fun QualityListItem(
    text: String,
    description: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = {
            Text(
                text = text,
                fontSize = 15.sp
            )
        },
        supportingContent = { Text(description) },
        leadingContent = {
            Icon(
                tint = MaterialTheme.colorScheme.primary,
                imageVector = icon,
                contentDescription = description
            )
        },
        trailingContent = {
            RadioButton(
                selected = selected,
                onClick = { onClick() })
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun AnimeSkipDialog(visibilityState: MutableState<Boolean>) {
    val context = LocalContext.current
    val clientKey =
        AppDataStore.read(context, DataStoreScheme.FIELD_ANIMESKIP_USER_CLIENT_ID)
            .collectAsState(initial = "")
    val coroutineScope = rememberCoroutineScope()

    if (visibilityState.value) {
        AlertDialog(
            onDismissRequest = {
                visibilityState.value = false
            },
            confirmButton = {
                TextButton(onClick = { visibilityState.value = false }) {
                    Text(text = stringResource(id = R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        visibilityState.value = false
                        AppDataStore.write(
                            context,
                            DataStoreScheme.FIELD_ANIMESKIP_USER_CLIENT_ID,
                            ""
                        )
                        AppDataStore.write(context, DataStoreScheme.FIELD_USE_ANIMESKIP.key, false)
                    }
                }) {
                    Text(text = stringResource(id = R.string.logout), color = Color.Red)
                }
            },
            icon = {
                Icon(
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary,
                    imageVector = Icons.Outlined.Key,
                    contentDescription = "key"
                )
            },
            title = { Text(stringResource(id = R.string.animeskip_client_key)) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = stringResource(id = R.string.animeskip_client_key_desc))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        content = {
                            OutlinedTextField(
                                value = clientKey.value.toString(),
                                onValueChange = {},
                                enabled = false,
                                label = { Text(text = stringResource(id = R.string.anime_skip_key_label)) }
                            )
                        }
                    )
                }
            }
        )
    }

}

@Composable
fun InstantSeekDialog(visibilityState: MutableState<Boolean>) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val field = DataStoreScheme.FIELD_INSTANT_SEEK_TIME

    val instantSeekFlowState = AppDataStore.read(context, field.key).collectAsState(
            initial = field.defaultValue
        )
    val currentTime = remember(instantSeekFlowState.value) {
        instantSeekFlowState.value ?: field.defaultValue
    }

    if (visibilityState.value) {
        AlertDialog(
            onDismissRequest = {
                visibilityState.value = false
            },
            confirmButton = {
                TextButton(onClick = { visibilityState.value = false }) {
                    Text(text = stringResource(id = R.string.confirm))
                }
            },
            icon = {
                Icon(
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary,
                    imageVector = Icons.Outlined.FastForward,
                    contentDescription = "fast forward"
                )
            },
            title = { Text(stringResource(id = R.string.instant_seek_time)) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = stringResource(id = R.string.instant_seek_time_desc))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        content = {
                            Slider(
                                value = currentTime.toFloat(),
                                onValueChange = {
                                    coroutineScope.launch {
                                        AppDataStore.write(context, field.key, it.roundToInt())
                                    }
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.secondary,
                                    activeTrackColor = MaterialTheme.colorScheme.secondary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                                ),
                                steps = 6,
                                valueRange = 3f..20f
                            )
                            Text(
                                text = pluralStringResource(
                                    id = R.plurals.seek_plurals,
                                    count = currentTime, currentTime
                                ), color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    )
                }
            }
        )
    }
}