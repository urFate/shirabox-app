package live.shirabox.shirabox.ui.activity.settings

import androidx.datastore.preferences.core.booleanPreferencesKey

object SettingsScheme {
    const val DATASTORE_NAME = "settings"

    val FIELD_SUBSCRIPTION = booleanPreferencesKey("notifications_sub")
    val FIELD_DARK_MODE = booleanPreferencesKey("dark_mode")
    val FIELD_USER_COLOR_PALETTE = booleanPreferencesKey("user_colors")
}