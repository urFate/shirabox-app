package org.shirabox.app.ui.activity.settings.category.playback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.shirabox.app.R
import org.shirabox.core.datastore.AppDataStore
import org.shirabox.core.datastore.DataStoreScheme
import kotlin.math.roundToInt

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
                TextButton(
                    shape = RoundedCornerShape(32),
                    onClick = { visibilityState.value = false }
                ) {
                    Text(text = stringResource(id = R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(
                    shape = RoundedCornerShape(32),
                    onClick = {
                        coroutineScope.launch {
                            visibilityState.value = false
                            AppDataStore.write(
                                context,
                                DataStoreScheme.FIELD_ANIMESKIP_USER_CLIENT_ID,
                                "",
                            )
                            AppDataStore.write(
                                context,
                                DataStoreScheme.FIELD_USE_ANIMESKIP.key,
                                false,
                            )
                        }
                    },
                ) {
                    Text(text = stringResource(id = R.string.logout), color = Color.Red)
                }
            },
            icon = {
                Icon(
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary,
                    painter = painterResource(R.drawable.lock_keyhole),
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
                TextButton(
                    shape = RoundedCornerShape(32),
                    onClick = { visibilityState.value = false }
                ) {
                    Text(text = stringResource(id = R.string.confirm))
                }
            },
            icon = {
                Icon(
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary,
                    painter = painterResource(R.drawable.forward),
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