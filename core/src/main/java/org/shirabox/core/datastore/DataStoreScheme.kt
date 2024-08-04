package org.shirabox.core.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey


object DataStoreScheme {
    const val DATASTORE_NAME = "settings"

    /**
     * Preferences fields
     */
    val FIELD_SUBSCRIPTION = PreferenceField(booleanPreferencesKey("notifications_sub"), true)
    val FIELD_DARK_MODE = PreferenceField(booleanPreferencesKey("dark_mode"), false)
    val FIELD_DYNAMIC_COLOR = PreferenceField(booleanPreferencesKey("user_colors"), true)
    val FIELD_DEFAULT_QUALITY = PreferenceField(intPreferencesKey("default_quality"), 1080)
    val FIELD_OPENING_SKIP = PreferenceField(booleanPreferencesKey("opening_skip"), true)
    val FIELD_USE_ANIMESKIP = PreferenceField(booleanPreferencesKey("use_anime_skip"), false)
    val FIELD_INSTANT_SEEK_TIME = PreferenceField(intPreferencesKey("instant_seek_time"), 10)

    /**
     * Other fields
     */
    val FIELD_ANIMESKIP_USER_CLIENT_ID = stringPreferencesKey("animeskip_client_id")
    val FIELD_SCHEDULE_DIALOG_CONFIRMATION = booleanPreferencesKey("schedule_dialog_confirmation")
}