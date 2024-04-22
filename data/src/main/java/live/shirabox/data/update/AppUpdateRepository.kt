package live.shirabox.data.update

import fuel.httpGet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

object AppUpdateRepository {
    private const val API_ENDPOINT = "https://api.shirabox.live/v1"
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    fun checkAppUpdates(currentVersionTag: String): Flow<AppUpdateState> = flow {
        try {
            val response = "$API_ENDPOINT/downloads/update".httpGet(listOf(
                "tag" to currentVersionTag
            ))

            emit(json.decodeFromString<AppUpdateState>(response.body))
        } catch (ex: Exception) { throw ex }
    }
}