package live.shirabox.shirabox.ui.activity.settings.category

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import live.shirabox.shirabox.R
import live.shirabox.shirabox.ui.activity.settings.Preference
import live.shirabox.shirabox.ui.activity.settings.SettingsScheme
import live.shirabox.shirabox.ui.activity.settings.SettingsViewModel
import live.shirabox.shirabox.ui.activity.settings.SwitchPreference


@Composable
fun GeneralSettingsScreen(viewModel: SettingsViewModel) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Preference(
            title = stringResource(id = R.string.settings_notifications),
            description = stringResource(
                id = R.string.notifications_settings_description
            ),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "notifications"
                )
            }
        ) {
            val intent = Intent().apply {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }

                    else -> {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        addCategory(Intent.CATEGORY_DEFAULT)
                        data = Uri.parse("package:" + context.packageName)
                    }
                }
            }

            context.startActivity(intent)
        }

        SwitchPreference(
            title = { Text(text = stringResource(id = R.string.notifications_subscription_settings)) },
            description = stringResource(
                id = R.string.notifications_subscription_settings_description
            ),
            model = viewModel,
            key = SettingsScheme.FIELD_SUBSCRIPTION
        )
    }
}