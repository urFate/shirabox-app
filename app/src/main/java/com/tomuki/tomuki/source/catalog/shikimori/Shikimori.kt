package com.tomuki.tomuki.source.catalog.shikimori

import android.net.Uri
import com.tomuki.tomuki.model.Content
import com.tomuki.tomuki.model.ContentType
import com.tomuki.tomuki.model.Rating
import com.tomuki.tomuki.source.catalog.AbstractCatalog
import kotlinx.serialization.json.Json

class Shikimori : AbstractCatalog("Shikimori", "https://shikimori.me") {

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
                val data = json.decodeFromString<LibraryAnimeInitiator>(response)

                data.libraryAnimeData.map {
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
                val data = json.decodeFromString<LibraryBookInitiator>(response)

                data.libraryBookData.map {
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
        val response = httpGET("$url/api/$section?limit=12&page=$page".plus {
            var str = ""

            query.forEach {
                str += "&${it.key}=${it.value}"
            }
        }) ?: return emptyList()

        return when (section) {
            "animes" -> {
                val data = json.decodeFromString<LibraryAnimeInitiator>(response)

                data.libraryAnimeData.map {
                    Content(
                        name = it.russian,
                        altName = it.name,
                        coverUri = "$url/$it.image.original",
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
                val data = json.decodeFromString<LibraryBookInitiator>(response)

                data.libraryBookData.map {
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