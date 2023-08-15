package com.shirabox.shirabox.source.content.manga.remanga

import android.net.Uri
import com.shirabox.shirabox.model.ContentType
import com.shirabox.shirabox.model.Episode
import com.shirabox.shirabox.model.EpisodesInfo
import com.shirabox.shirabox.source.content.AbstractContentSource
import java.text.SimpleDateFormat
import java.util.Locale

object Remanga : AbstractContentSource (
    "ReManga",
    "https://api.remanga.org",
    ContentType.MANGA,
    "https://remanga.org/apple-touch-icon.png"
) {
    override suspend fun searchEpisodes(query: String): List<Episode> {
        return search(query)?.content?.firstOrNull()?.dir?.let { fetchBookChapters(it, false) }
            ?: emptyList()
    }

    override suspend fun searchEpisodesInfo(query: String): EpisodesInfo? {
        return search(query)?.content?.firstOrNull()?.dir?.let { fetchBookChapters(it, true) }
            ?.map {
                EpisodesInfo(it.episode, it.uploadTimestamp)
            }?.firstOrNull()
    }

    private fun search(query: String) : LibraryWrapperData<SearchBookData>? {
        val response = httpGET("$url/api/search/?query=${Uri.encode(query)}")
            ?: return null

        return json.decodeFromString<LibraryWrapperData<SearchBookData>>(response)
    }

    private fun fetchBookChapters(dir: String, minimal: Boolean): List<Episode> {
        val response = httpGET("$url/api/titles/$dir") ?: return emptyList()
        val bookData = json.decodeFromString<WrapperData<BookData>>(response).content

        val branchId = bookData.branches.first().id
        val chaptersCount = if(minimal) 1 else bookData.branches.first().countChapters

        val chaptersResponse = httpGET("$url/api/titles/chapters/" +
                "?count=$chaptersCount&branch_id=$branchId")
            ?: return emptyList()

        val chaptersData =
            json.decodeFromString<LibraryWrapperData<ChapterData>>(chaptersResponse).content

        // For API v23 compatibility
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())

        return chaptersData.map {
            Episode(
                name = it.name,
                episode = it.index,
                uploadTimestamp = (dateFormat.parse(it.uploadDate)?.time ?: 0),
                chapters = fetchChapterPages(it.id),
                type = this.contentType
            )
        }
    }

    private fun fetchChapterPages(id: Int): List<String> {
        val response = httpGET("$url/api/titles/chapters/$id")
            ?: return emptyList()

        val chapterData =
            json.decodeFromString<WrapperData<ChapterPagesData>>(response).content

        return chapterData.pages.flatten().map { it.img }
    }
}