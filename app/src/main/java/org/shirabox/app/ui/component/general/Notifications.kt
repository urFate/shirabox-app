package org.shirabox.app.ui.component.general

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.shirabox.app.R

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
                    imageVector = Icons.Outlined.NotificationsActive,
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
                    imageVector = Icons.Outlined.NotificationsOff,
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