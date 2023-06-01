package com.tomuki.tomuki.source.catalog

import com.tomuki.tomuki.model.Content
import com.tomuki.tomuki.model.ContentType
import com.tomuki.tomuki.util.Values
import okhttp3.OkHttpClient
import okhttp3.Request

abstract class AbstractCatalog internal constructor(
    val name: String, val url: String, private val client: OkHttpClient = OkHttpClient()
) {
    abstract suspend fun fetchOngoings(page: Int, type: ContentType): List<Content>
    abstract suspend fun fetchPopulars(page: Int, type: ContentType): List<Content>

    abstract suspend fun fetchContent(id: Int, type: ContentType): Content?
    abstract suspend fun search(query: String, type: ContentType): List<Content>

    fun httpGET(url: String): String? {
        val request = Request.Builder().apply {
            this.url(url)
            this.header("User-Agent", Values.USER_AGENT)
        }.build()

        client.newCall(request).execute().use {
            if (!it.isSuccessful) return null

            return it.body?.string()
        }
    }
}