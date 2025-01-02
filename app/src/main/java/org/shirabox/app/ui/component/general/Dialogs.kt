package org.shirabox.app.ui.component.general

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.shirabox.app.R
import org.shirabox.core.model.Quality

@Composable
fun NotificationsRequestDialog(isOpen: MutableState<Boolean>, onConfirm: () -> Unit) {
    if(isOpen.value) {
        AlertDialog(
            onDismissRequest = {
                isOpen.value = false
            },
            icon = {
                Icon(
                    modifier = Modifier.size(48.dp),
                    painter = painterResource(R.drawable.reception_bell),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = null
                )
            },
            title = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.notifications_request),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    stringResource(R.string.notifications_request_text)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isOpen.value = false
                        onConfirm()
                    }
                ) {
                    Text(stringResource(R.string.enable_notifications))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        isOpen.value = false
                    }
                ) {
                    Text(stringResource(R.string.disable_notifications))
                }
            }
        )
    }
}

@Composable
fun NotificationsDismissDialog(context: Context, isOpen: MutableState<Boolean>) {
    if(isOpen.value) {
        AlertDialog(
            onDismissRequest = {
                isOpen.value = false
            },
            icon = {
                Icon(
                    modifier = Modifier.size(48.dp),
                    painter = painterResource(R.drawable.bell_slash),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = null
                )
            },
            title = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.notifications_disabled),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    stringResource(R.string.notifications_disabled_text)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isOpen.value = false
                    }
                ) {
                    Text(stringResource(R.string.notifications_disabled_confirm))
                }
            },
            dismissButton = {
                TextButton(
                        onClick = {
                            isOpen.value = false
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.parse("package:${context.applicationContext.packageName}")
                            }
                            context.startActivity(intent)
                        }
                        ) {
                    Text(stringResource(R.string.notifications_request))
                }
            }
        )
    }
}

@Composable
fun DisposableScheduleDialog(isOpen: MutableState<Boolean>, onConfirm: () -> Unit) {
    if(isOpen.value) {
        AlertDialog(
            onDismissRequest = {
                isOpen.value = false
            },
            icon = {
                Icon(
                    modifier = Modifier.size(48.dp),
                    painter = painterResource(R.drawable.calendar_check),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = null
                )
            },
            title = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.schedule_dialog_title),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    stringResource(R.string.schedule_dialog_text)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isOpen.value = false
                        onConfirm()
                    }
                ) {
                    Text(stringResource(R.string.schedule_dialog_confirm))
                }
            }
        )
    }
}

@Composable
fun QualityDialog(
    title: String,
    description: String,
    icon: ImageVector,
    visibilityState: MutableState<Boolean>,
    maxQuality: Quality,
    autoSelect: Quality,
    onConfirm: (quality: Quality) -> Unit
) {
    if (visibilityState.value) {
        val selectedQuality = remember { mutableStateOf(autoSelect) }

        AlertDialog(
            onDismissRequest = {
                visibilityState.value = false
            },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm(selectedQuality.value)
                    visibilityState.value = false
                }) {
                    Text(text = stringResource(id = R.string.confirm))
                }
            },
            icon = {
                Icon(
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary,
                    imageVector = icon,
                    contentDescription = "quality"
                )
            },
            title = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = title,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = description,
                        textAlign = TextAlign.Center
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        content = {
                            Quality.qualities
                                .filter { it <= maxQuality }
                                .sortedDescending()
                                .forEach {
                                    QualityListItem(
                                        text = when (it) {
                                            Quality.SD -> stringResource(id = R.string.low_quality)
                                            Quality.HD -> stringResource(id = R.string.medium_quality)
                                            Quality.FHD -> stringResource(id = R.string.high_quality)
                                        },
                                        icon = ImageVector.vectorResource(
                                            when (it) {
                                                Quality.SD -> R.drawable.badge_sd
                                                Quality.HD -> R.drawable.badge_hd
                                                Quality.FHD -> R.drawable.badge_fhd
                                            }
                                        ),
                                        description = "${it.quality}p",
                                        selected = (selectedQuality.value == it)
                                    ) {
                                        selectedQuality.value = it
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
    androidx.compose.material3.ListItem(
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