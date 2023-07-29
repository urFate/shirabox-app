package com.shirabox.shirabox.source.content

import android.util.Log
import com.shirabox.shirabox.model.ContentType
import com.shirabox.shirabox.model.Episode
import com.shirabox.shirabox.model.EpisodesInfo
import com.shirabox.shirabox.model.Quality
import com.shirabox.shirabox.util.Values
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException

abstract class AbstractContentSource (
    val name: String,
    val url: String,
    val contentType: ContentType,
    val icon: String? = null,
    ) {

    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    abstract suspend fun searchEpisodes(query: String, videoQuality: Quality? = null): List<Episode>
    abstract suspend fun searchEpisodesInfo(query: String): EpisodesInfo?

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
            Log.e("HTTP GET", "Failed to GET $url: ${response.statusCode()} - ${response.statusMessage()}")
            return null
        }

        return document.wholeText()
    }

}