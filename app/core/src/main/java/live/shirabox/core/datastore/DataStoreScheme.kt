package live.shirabox.core.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey


object DataStoreScheme {
    const val DATASTORE_NAME = "settings"

    val FIELD_SUBSCRIPTION = DataStoreField(booleanPreferencesKey("notifications_sub"), true)
    val FIELD_DARK_MODE = DataStoreField(booleanPreferencesKey("dark_mode"), false)
    val FIELD_DYNAMIC_COLOR = DataStoreField(booleanPreferencesKey("user_colors"), true)
    val FIELD_DEFAULT_QUALITY = DataStoreField(intPreferencesKey("default_quality"), 1080)
    val FIELD_OPENING_SKIP = DataStoreField(booleanPreferencesKey("opening_skip"), true)
}