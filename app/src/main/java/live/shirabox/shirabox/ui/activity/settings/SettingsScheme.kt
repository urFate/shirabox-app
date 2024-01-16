package live.shirabox.shirabox.ui.activity.settings

import android.os.Build
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import live.shirabox.shirabox.R

object SettingsScheme {
    const val DATASTORE_NAME = "settings"

    val FIELD_SUBSCRIPTION = booleanPreferencesKey("notifications_sub")
    val FIELD_DARK_MODE = booleanPreferencesKey("dark_mode")
    val FIELD_USER_COLOR_PALETTE = booleanPreferencesKey("user_colors")
}