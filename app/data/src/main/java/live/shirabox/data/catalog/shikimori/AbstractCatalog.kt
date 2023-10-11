package live.shirabox.data.catalog.shikimori

import live.shirabox.core.model.Content
import live.shirabox.core.model.ContentType

abstract class AbstractCatalog internal constructor(
    val name: String, val url: String
) {
    abstract suspend fun fetchOngoings(page: Int, type: ContentType): List<Content>
    abstract suspend fun fetchPopulars(page: Int, type: ContentType): List<Content>

    abstract suspend fun fetchContent(id: Int, type: ContentType): Content?
    abstract suspend fun search(query: String, type: ContentType): List<Content>

    abstract suspend fun fetchRelated(id: Int, type: ContentType): List<Content?>
}