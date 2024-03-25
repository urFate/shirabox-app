package live.shirabox.data.content

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import live.shirabox.core.entity.EpisodeEntity
import live.shirabox.core.model.Content
import live.shirabox.core.model.ContentType

abstract class AbstractContentRepository (
    val name: String,
    val url: String,
    val contentType: ContentType,
    val icon: String? = null,
    ) {
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    abstract suspend fun searchEpisodes(content: Content): Flow<List<EpisodeEntity>>

}