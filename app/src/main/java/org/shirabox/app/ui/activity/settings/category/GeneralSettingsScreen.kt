package org.shirabox.app.ui.activity.settings.category

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import org.shirabox.app.R
import org.shirabox.app.ui.activity.settings.Preference
import org.shirabox.app.ui.activity.settings.SwitchPreference
import org.shirabox.core.datastore.DataStoreScheme


@Composable
fun GeneralSettingsScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Preference(
            headlineContent = { Text(stringResource(id = R.string.settings_notifications)) },
            supportingContent = {
                Text(
                    text = stringResource(id = R.string.notifications_settings_description),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            },
            leadingContent = {
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
            headlineContent = { Text(text = stringResource(id = R.string.notifications_subscription_settings)) },
            supportingContent = {
                Text(
                    text = stringResource(id = R.string.notifications_subscription_settings_description),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.Subscriptions,
                    contentDescription = "subscribe",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            dsField = DataStoreScheme.FIELD_SUBSCRIPTION
        )
    }
}