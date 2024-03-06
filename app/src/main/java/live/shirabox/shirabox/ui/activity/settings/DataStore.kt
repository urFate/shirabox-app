package live.shirabox.shirabox.ui.activity.settings

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import live.shirabox.shirabox.ui.activity.dataStore
import java.io.IOException

fun dataStoreEmptyStateFlow(context: Context) : Flow<Boolean> {
    return context.dataStore.data.catch {
        if (it is IOException) {
            it.printStackTrace()
            emit(emptyPreferences())
        } else {
            throw it
        }
    }.map {
        it.asMap().isEmpty()
    }
}

suspend fun resetDataStore(context: Context) {
    Log.i("DataStore", "Resetting datastore values...")
    context.dataStore.edit {
        SettingsScheme.BOOL_DEFAULTS.forEach { entry ->
            it[entry.key] = entry.value
        }
        SettingsScheme.INT_DEFAULTS.forEach{entry ->
            it[entry.key] = entry.value
        }
    }
}