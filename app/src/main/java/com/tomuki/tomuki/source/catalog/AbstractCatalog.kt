package com.tomuki.tomuki.source.catalog

import android.util.Log
import com.tomuki.tomuki.model.Content
import com.tomuki.tomuki.model.ContentType
import com.tomuki.tomuki.util.Values
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException

abstract class AbstractCatalog internal constructor(
    val name: String, val url: String, private val client: OkHttpClient = OkHttpClient()
) {
    abstract suspend fun fetchOngoings(page: Int, type: ContentType): List<Content>
    abstract suspend fun fetchPopulars(page: Int, type: ContentType): List<Content>

    abstract suspend fun fetchContent(id: Int, type: ContentType): Content?
    abstract suspend fun search(query: String, type: ContentType): List<Content>

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