package live.shirabox.shirabox.ui.activity.settings

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey


object SettingsScheme {
    const val DATASTORE_NAME = "settings"

    val FIELD_SUBSCRIPTION = booleanPreferencesKey("notifications_sub")
    val FIELD_DARK_MODE = booleanPreferencesKey("dark_mode")
    val FIELD_USER_COLOR_PALETTE = booleanPreferencesKey("user_colors")
    val FIELD_DEFAULT_QUALITY = intPreferencesKey("default_quality")
    val FIELD_OPENING_SKIP = booleanPreferencesKey("opening_skip")

    val BOOL_DEFAULTS = mapOf(
        Pair(FIELD_SUBSCRIPTION, true),
        Pair(FIELD_DARK_MODE, false),
        Pair(FIELD_USER_COLOR_PALETTE, true),
        Pair(FIELD_OPENING_SKIP, false)
    )

    val INT_DEFAULTS = mapOf(
        Pair(FIELD_DEFAULT_QUALITY, 1080)
    )
}