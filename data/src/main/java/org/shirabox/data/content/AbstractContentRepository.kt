package org.shirabox.data.content

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import org.shirabox.core.entity.EpisodeEntity
import org.shirabox.core.model.Content
import org.shirabox.core.model.ContentType
import org.shirabox.core.model.StreamProtocol

abstract class AbstractContentRepository (
    val name: String,
    val url: String,
    val contentType: ContentType,
    val streamingType: StreamProtocol
) {
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    abstract suspend fun searchEpisodes(content: Content): Flow<List<EpisodeEntity>>
    abstract suspend fun searchEpisodesInRange(content: Content, range: IntRange): Flow<List<EpisodeEntity>>
}