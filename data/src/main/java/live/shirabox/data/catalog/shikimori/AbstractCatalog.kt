package live.shirabox.data.catalog.shikimori

import kotlinx.coroutines.flow.Flow
import live.shirabox.core.model.Content
import live.shirabox.core.model.ContentType

abstract class AbstractCatalog internal constructor(
    val name: String, val url: String
) {
    abstract fun fetchOngoings(page: Int, type: ContentType): Flow<List<Content>>
    abstract fun fetchPopulars(pages: IntRange, type: ContentType): Flow<List<Content>>

    abstract suspend fun fetchContent(id: Int, type: ContentType): Content?
    abstract fun search(query: String, type: ContentType): Flow<List<Content>>

    abstract suspend fun fetchRelated(id: Int, type: ContentType): List<Content?>
}