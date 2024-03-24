package live.shirabox.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore by preferencesDataStore(
    name = DataStoreScheme.DATASTORE_NAME
)

object AppDataStore {
    fun <T : Any> read(context: Context, key: Preferences.Key<T>): Flow<T?> {
        return context.dataStore.data.catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else throw it
        }.map { it[key] }
    }

    suspend fun <T : Any> write(context: Context, key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { it[key] = value }
    }
}