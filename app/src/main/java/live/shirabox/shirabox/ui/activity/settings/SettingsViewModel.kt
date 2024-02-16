package live.shirabox.shirabox.ui.activity.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore(
    name = SettingsScheme.DATASTORE_NAME
)

class SettingsViewModel : ViewModel() {

    fun booleanPreferenceFlow(context: Context, key: Preferences.Key<Boolean>): Flow<Boolean> {
        return context.dataStore.data.catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map {
            it[key] ?: false
        }
    }

    fun intPreferenceFlow(context: Context, key: Preferences.Key<Int>): Flow<Int> {
        return context.dataStore.data.catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map {
            it[key] ?: 0
        }
    }

    suspend fun writeBooleanData(context: Context, key: Preferences.Key<Boolean>, value: Boolean) {
        context.dataStore.edit { settings ->
            settings[key] = value
        }
    }

    suspend fun writeIntData(context: Context, key: Preferences.Key<Int>, value: Int) {
        context.dataStore.edit { settings ->
            settings[key] = value
        }
    }

}