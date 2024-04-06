package live.shirabox.data.catalog.shikimori

import kotlinx.coroutines.flow.Flow
import live.shirabox.core.model.Content
import live.shirabox.core.model.ContentType
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

abstract class AbstractCatalogRepository internal constructor(
    val name: String, val url: String
) {
    val myClient: OkHttpClient = OkHttpClient.Builder().apply {
        connectTimeout(10, TimeUnit.SECONDS)
        writeTimeout(5, TimeUnit.SECONDS)
        readTimeout(5, TimeUnit.SECONDS)
    }.build()

    abstract fun fetchOngoings(page: Int, type: ContentType): Flow<List<Content>>
    abstract fun fetchPopulars(pages: IntRange, type: ContentType): Flow<List<Content>>

    abstract fun fetchContent(id: Int, type: ContentType): Flow<Content>
    abstract fun search(query: String, type: ContentType): Flow<List<Content>>

    abstract fun fetchRelated(id: Int, type: ContentType): Flow<List<Content>>
}