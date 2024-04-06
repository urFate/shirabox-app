package live.shirabox.core.datastore

import androidx.datastore.preferences.core.Preferences

data class PreferenceField <T> (
    val key: Preferences.Key<T>,
    val defaultValue: T
)
