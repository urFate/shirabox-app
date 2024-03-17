package live.shirabox.data.content.manga.remanga

import fuel.httpGet
import live.shirabox.core.entity.EpisodeEntity
import live.shirabox.core.model.Content
import live.shirabox.core.model.ContentType
import live.shirabox.data.content.AbstractContentSource
import java.text.SimpleDateFormat
import java.util.Locale

object Remanga : AbstractContentSource(
    "ReManga",
    "https://api.remanga.org",
    ContentType.MANGA,
    "https://remanga.org/apple-touch-icon.png"
) {
    override suspend fun searchEpisodes(content: Content): List<EpisodeEntity> {
        return search(content.name).content.firstOrNull()?.dir?.let { fetchBookChapters(it, false) }
            ?: emptyList()
    }

    private suspend fun search(query: String) : LibraryWrapperData<SearchBookData> {
        val response = "$url/api/search".httpGet(listOf("query" to query)).body

        return json.decodeFromString<LibraryWrapperData<SearchBookData>>(response)
    }

    private suspend fun fetchBookChapters(dir: String, minimal: Boolean): List<EpisodeEntity> {
        val response = "$url/api/titles/$dir".httpGet().body
        val bookData = json.decodeFromString<WrapperData<BookData>>(response).content

        val branchId = bookData.branches.first().id
        val chaptersCount = if (minimal) 1 else bookData.branches.first().countChapters

        val chaptersResponse = "$url/api/titles/chapters/".httpGet(
            listOf(
                "count" to "$chaptersCount",
                "branch_id" to "$branchId"
            )
        ).body

        val chaptersData =
            json.decodeFromString<LibraryWrapperData<ChapterData>>(chaptersResponse).content

        // For API v23 compatibility
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())

        return chaptersData.map {
            EpisodeEntity(
                name = it.name,
                source = this.name,
                episode = it.index,
                uploadTimestamp = (dateFormat.parse(it.uploadDate)?.time ?: 0),
                pages = fetchChapterPages(it.id),
                type = this.contentType
            )
        }
    }

    private suspend fun fetchChapterPages(id: Int): List<String> {
        val response = "$url/api/titles/chapters/$id".httpGet().body

        val chapterData =
            json.decodeFromString<WrapperData<ChapterPagesData>>(response).content

        return chapterData.pages.flatten().map { it.img }
    }
}