package com.shirabox.shirabox.source.catalog.shikimori

import android.net.Uri
import com.shirabox.shirabox.model.Content
import com.shirabox.shirabox.model.ContentType
import com.shirabox.shirabox.model.Rating
import com.shirabox.shirabox.source.catalog.AbstractCatalog
import kotlinx.serialization.json.Json

object Shikimori : AbstractCatalog("Shikimori", "https://shikimori.me") {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    override suspend fun fetchOngoings(page: Int, type: ContentType): List<Content> {
        return when (type) {
            ContentType.ANIME -> fetchCatalogContent(
                "animes", page, mapOf(
                    "status" to "ongoing", "order" to "ranked"
                )
            )

            ContentType.MANGA -> fetchCatalogContent(
                "mangas", page, mapOf(
                    "status" to "ongoing", "order" to "ranked"
                )
            )

            ContentType.RANOBE -> fetchCatalogContent(
                "ranobe", page, mapOf(
                    "status" to "ongoing", "order" to "ranked"
                )
            )
        }
    }

    override suspend fun fetchPopulars(page: Int, type: ContentType): List<Content> {
        return when(type) {
            ContentType.ANIME -> fetchCatalogContent(
                "animes", page, mapOf(
                    "order" to "popularity"
                )
            )

            ContentType.MANGA -> fetchCatalogContent(
                "mangas", page, mapOf(
                    "order" to "popularity"
                )
            )

            ContentType.RANOBE -> fetchCatalogContent(
                "ranobe", page, mapOf(
                    "order" to "popularity"
                )
            )
        }
    }

    override suspend fun fetchContent(id: Int, type: ContentType): Content? {

        val response = httpGET("$url/api/${sectionFromType(type)}/$id") ?: return null

        return when (type) {
            ContentType.ANIME -> {
                val data = json.decodeFromString<AnimeData>(response)

                Content(name = data.russian,
                    altName = data.name,
                    description = data.description,
                    coverUri = "$url/${data.image.original}",
                    production = data.studios.first().name,
                    releaseYear = data.airedOn.substring(0..3),
                    type = type,
                    kind = data.kind,
                    episodesCount = data.episodes,
                    rating = Rating(data.score.toDouble(), data.ratesScoresStats.associate {
                        it.name to it.value
                    }),
                    shikimoriID = data.id,
                    genres = data.genres.map { it.name }
                )
            }

            ContentType.MANGA, ContentType.RANOBE -> {
                val data = json.decodeFromString<BookData>(response)

                Content(name = data.russian,
                    altName = data.name,
                    description = data.description,
                    coverUri = "$url/${data.image.original}",
                    production = data.publishers.first().name,
                    releaseYear = data.airedOn.substring(0..3),
                    type = type,
                    kind = data.kind,
                    episodesCount = data.chapters,
                    rating = Rating(data.score.toDouble(), data.ratesScoresStats.associate {
                        it.name to it.value
                    }),
                    shikimoriID = data.id,
                    genres = data.genres.map { it.name }
                )
            }
        }
    }

    override suspend fun search(query: String, type: ContentType): List<Content> {
        val response = httpGET("$url/api/${sectionFromType(type)}&search=${Uri.encode(query)}")
            ?: return emptyList()

        return when (type) {
            ContentType.ANIME -> {
                val data = json.decodeFromString<List<LibraryAnimeData>>(response)

                data.map {
                    Content(
                        name = it.russian,
                        altName = it.name,
                        coverUri = "$url/${it.image.original}",
                        releaseYear = it.airedOn.substring(0..3),
                        type = type,
                        kind = it.kind,
                        episodesCount = it.episodes,
                        rating = Rating(it.score.toDouble()),
                        shikimoriID = it.id
                    )
                }
            }

            ContentType.MANGA, ContentType.RANOBE -> {
                val data = json.decodeFromString<List<LibraryBookData>>(response)

                data.map {
                    Content(
                        name = it.russian,
                        altName = it.name,
                        coverUri = "$url/${it.image.original}",
                        releaseYear = it.airedOn.substring(0..3),
                        type = type,
                        kind = it.kind,
                        episodesCount = it.chapters,
                        rating = Rating(it.score.toDouble()),
                        shikimoriID = it.id
                    )
                }
            }
        }
    }


    private fun fetchCatalogContent(
        section: String, page: Int, query: Map<String, String>
    ): List<Content> {
        val response = httpGET("$url/api/$section?limit=16&page=$page&".plus(
            query.entries.map {
                "${it.key}=${it.value}"
            }.reduce { acc, s ->
                "$acc&$s"
            }
        )) ?: return emptyList()

        return when (section) {
            "animes" -> {
                val data = json.decodeFromString<List<LibraryAnimeData>>(response)

                data.map {
                    Content(
                        name = it.russian,
                        altName = it.name,
                        coverUri = "$url/${it.image.original}",
                        releaseYear = it.airedOn.substring(0..3),
                        type = ContentType.ANIME,
                        kind = it.kind,
                        episodesCount = it.episodes,
                        rating = Rating(it.score.toDouble()),
                        shikimoriID = it.id
                    )
                }
            }

            "mangas", "ranobe" -> {
                val data = json.decodeFromString<List<LibraryBookData>>(response)

                data.map {
                    Content(
                        name = it.russian,
                        altName = it.name,
                        coverUri = "$url/${it.image.original}",
                        releaseYear = it.airedOn.substring(0..3),
                        type = ContentType.fromString(section),
                        kind = it.kind,
                        episodesCount = it.chapters,
                        rating = Rating(it.score.toDouble()),
                        shikimoriID = it.id
                    )
                }
            }

            else -> emptyList()
        }
    }

    private fun sectionFromType(contentType: ContentType): String {
        return when (contentType) {
            ContentType.MANGA -> "mangas"
            ContentType.RANOBE -> "ranobe"
            ContentType.ANIME -> "animes"
        }
    }
}