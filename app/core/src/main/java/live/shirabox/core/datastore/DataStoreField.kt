package live.shirabox.core.datastore

import androidx.datastore.preferences.core.Preferences

data class DataStoreField <T> (
    val key: Preferences.Key<T>,
    val defaultValue: T
)
