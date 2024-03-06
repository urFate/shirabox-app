package live.shirabox.data.content

import kotlinx.serialization.json.Json
import live.shirabox.core.entity.EpisodeEntity
import live.shirabox.core.model.ContentType
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

abstract class AbstractContentSource (
    val name: String,
    val url: String,
    val contentType: ContentType,
    val icon: String? = null,
    ) {

    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    val myClient = OkHttpClient().newBuilder()
        .connectTimeout(2L, TimeUnit.SECONDS)
        .readTimeout(2L, TimeUnit.SECONDS)
        .writeTimeout(2L, TimeUnit.SECONDS)
        .build()

    abstract suspend fun searchEpisodes(query: String): List<EpisodeEntity>
}