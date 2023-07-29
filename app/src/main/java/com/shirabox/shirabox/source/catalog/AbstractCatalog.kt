package com.shirabox.shirabox.source.catalog

import android.util.Log
import com.shirabox.shirabox.model.Content
import com.shirabox.shirabox.model.ContentType
import com.shirabox.shirabox.util.Values
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException

abstract class AbstractCatalog internal constructor(
    val name: String, val url: String
) {
    abstract suspend fun fetchOngoings(page: Int, type: ContentType): List<Content>
    abstract suspend fun fetchPopulars(page: Int, type: ContentType): List<Content>

    abstract suspend fun fetchContent(id: Int, type: ContentType): Content?
    abstract suspend fun search(query: String, type: ContentType): List<Content>

    abstract suspend fun fetchRelated(id: Int, type: ContentType): List<Content?>
    fun httpGET(url: String): String? {
        val document: Document = try {
            Jsoup.connect(url)
                .ignoreHttpErrors(true)
                .followRedirects(true)
                .ignoreContentType(true)
                .userAgent(Values.USER_AGENT)
                .timeout(15000)
                .get()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        val response = document.connection().response()
        if(response.statusCode() != 200) {
            Log.e("HttpGET", "Failed to GET $url: ${response.statusCode()} - ${response.statusMessage()}")
            return null
        }

        return document.wholeText()
    }
}