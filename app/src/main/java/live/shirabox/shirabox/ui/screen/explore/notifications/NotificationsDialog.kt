package live.shirabox.shirabox.ui.screen.explore.notifications

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import live.shirabox.shirabox.ui.component.general.NotificationsDismissDialog
import live.shirabox.shirabox.ui.component.general.NotificationsRequestDialog

@Composable
fun NotificationsDialog() {
    val context = LocalContext.current
    val activity = context as Activity

    val openRequestDialog = remember { mutableStateOf(false) }
    val openDismissDialog = remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            openDismissDialog.value = !isGranted
        }
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val showRationale = !ActivityCompat.shouldShowRequestPermissionRationale(
            activity, Manifest.permission.POST_NOTIFICATIONS)

        if(!isNotificationsPermissionGranted(context) && showRationale){
            openRequestDialog.value = true
        }

        NotificationsRequestDialog(isOpen = openRequestDialog) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    NotificationsDismissDialog(context = context, isOpen = openDismissDialog)
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun isNotificationsPermissionGranted(context: Context): Boolean =
    ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED