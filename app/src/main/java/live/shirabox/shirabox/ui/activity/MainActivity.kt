package live.shirabox.shirabox.ui.activity

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import live.shirabox.shirabox.ui.component.general.NotificationsDismissDialog
import live.shirabox.shirabox.ui.component.general.NotificationsRequestDialog
import live.shirabox.shirabox.ui.component.navigation.BottomNavigationView
import live.shirabox.shirabox.ui.theme.ShiraBoxTheme


class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val openDismissDialog = mutableStateOf(false)

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (!isGranted) {
                openDismissDialog.value = true
            }
        }

        setContent {
            ShiraBoxTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val openRequestDialog = remember {
                        mutableStateOf(false)
                    }

                    askNotificationPermission(
                        openDialogState = openRequestDialog,
                        launcher = requestPermissionLauncher
                    )
                    
                    NotificationsRequestDialog(isOpen = openRequestDialog) {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    NotificationsDismissDialog(isOpen = openDismissDialog)

                    BottomNavigationView()
                }
            }
        }
    }

    private fun askNotificationPermission(
        openDialogState: MutableState<Boolean>,
        launcher: ActivityResultLauncher<String>
    ) {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Ask user for notifications permission
                openDialogState.value = true
            } else {
                // Directly ask for the permission
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}