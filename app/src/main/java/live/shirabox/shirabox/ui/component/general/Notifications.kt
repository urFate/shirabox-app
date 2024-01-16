package live.shirabox.shirabox.ui.component.general

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import live.shirabox.shirabox.R

@Composable
fun NotificationsRequestDialog(isOpen: MutableState<Boolean>, onConfirm: () -> Unit) {
    if(isOpen.value) {
        AlertDialog(
            onDismissRequest = {
                isOpen.value = false
            },
            icon = { Icon(Icons.Outlined.NotificationsActive, contentDescription = null) },
            title = {
                Text(text = stringResource(R.string.notifications_request))
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
fun NotificationsDismissDialog(isOpen: MutableState<Boolean>) {
    if(isOpen.value) {
        AlertDialog(
            onDismissRequest = {
                isOpen.value = false
            },
            icon = { Icon(Icons.Outlined.NotificationsOff, contentDescription = null) },
            title = {
                Text(text = stringResource(R.string.notifications_disabled))
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
            }
        )
    }
}