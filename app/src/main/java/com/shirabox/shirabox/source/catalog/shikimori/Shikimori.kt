package com.shirabox.shirabox.source.catalog.shikimori

import android.net.Uri
import com.shirabox.shirabox.model.Content
import com.shirabox.shirabox.model.ContentType
import com.shirabox.shirabox.model.Rating
import com.shirabox.shirabox.source.catalog.AbstractCatalog
import com.shirabox.shirabox.util.Util
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
        return when (type) {
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
                    description = Util.decodeHtml(data.description.toString()),
                    image = "$url/${data.image.original}",
                    production = data.studios.first().name,
                    releaseYear = data.airedOn?.substring(0..3),
                    type = type,
                    kind = decodeKind(data.kind),
                    status = decodeStatus(data.status),
                    episodes = data.episodes,
                    episodesAired = data.episodesAired,
                    episodeDuration = data.duration.toInt(),
                    rating = Rating(data.score.toDouble(), data.ratesScoresStats.associate {
                        it.name to it.value
                    }),
                    shikimoriID = data.id,
                    genres = data.genres.map { it.russian }
                )
            }

            ContentType.MANGA, ContentType.RANOBE -> {
                val data = json.decodeFromString<BookData>(response)

                Content(name = data.russian,
                    altName = data.name,
                    description = Util.decodeHtml(data.description.toString()),
                    image = "$url/${data.image.original}",
                    production = data.publishers.firstOrNull()?.name ?: "",
                    releaseYear = data.airedOn?.substring(0..3) ?: "1997",
                    type = type,
                    kind = decodeKind(data.kind),
                    status = decodeStatus(data.status),
                    episodes = data.chapters,
                    rating = Rating(data.score.toDouble(), data.ratesScoresStats.associate {
                        it.name to it.value
                    }),
                    shikimoriID = data.id,
                    genres = data.genres.map { it.russian}
                )
            }
        }
    }

    override suspend fun search(query: String, type: ContentType): List<Content> {
        val response = httpGET("$url/api/${sectionFromType(type)}?search=${Uri.encode(query)}?limit=50")
            ?: return emptyList()

        return when (type) {
            ContentType.ANIME -> {
                val data = json.decodeFromString<List<LibraryAnimeData>>(response)

                data.map {
                    Content(
                        name = it.russian,
                        altName = it.name,
                        image = "$url/${it.image.original}",
                        releaseYear = it.airedOn.substring(0..3),
                        type = type,
                        kind = decodeKind(it.kind),
                        status = decodeStatus(it.status),
                        episodes = it.episodes,
                        episodesAired = it.episodesAired,
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
                        image = "$url/${it.image.original}",
                        releaseYear = it.airedOn.substring(0..3),
                        type = type,
                        kind = decodeKind(it.kind),
                        status = decodeStatus(it.status),
                        episodes = it.chapters,
                        rating = Rating(it.score.toDouble()),
                        shikimoriID = it.id
                    )
                }
            }
        }
    }

    override suspend fun fetchRelated(id: Int, type: ContentType): List<Content?> {
        val response =
            httpGET("$url/api/${sectionFromType(type)}/$id/related") ?: return emptyList()
        val data = json.decodeFromString<List<RelatedItem>>(response)

        return data.filter { it.relation != "Other" }.map {
            when {
                it.anime != null -> Content(
                    name = it.anime.russian,
                    altName = it.anime.name,
                    image = "$url/${it.anime.image.original}",
                    releaseYear = it.anime.releasedOn,
                    type = ContentType.ANIME,
                    kind = it.anime.kind.toString(),
                    status = it.anime.status,
                    episodes = it.anime.episodes,
                    episodesAired = it.anime.episodesAired,
                    rating = Rating(average = it.anime.score.toDouble(), scores = mapOf()),
                    shikimoriID = it.anime.id
                )

                it.manga != null -> Content(
                    name = it.manga.russian,
                    altName = it.manga.name,
                    image = "$url/${it.manga.image.original}",
                    releaseYear = it.manga.releasedOn ?: "",
                    type = ContentType.MANGA,
                    kind = it.manga.kind.toString(),
                    status = it.manga.status,
                    episodes = it.manga.chapters,
                    rating = Rating(average = it.manga.score.toDouble(), scores = mapOf()),
                    shikimoriID = it.manga.id
                )

                else -> null
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
                        image = "$url/${it.image.original}",
                        releaseYear = it.airedOn.substring(0..3),
                        type = ContentType.ANIME,
                        kind = decodeKind(it.kind),
                        status = decodeStatus(it.status),
                        episodes = it.episodes,
                        episodesAired = it.episodesAired,
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
                        image = "$url/${it.image.original}",
                        releaseYear = it.airedOn.substring(0..3),
                        type = ContentType.fromString(section),
                        kind = decodeKind(it.kind),
                        status = decodeStatus(it.status),
                        episodes = it.chapters,
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

    private fun decodeKind(str: String): String {
        return when(str) {
            "tv" -> "Сериал"
            "movie" -> "Фильм"
            "special" -> "Спешл"
            "manga" -> "Манга"
            "manhua" -> "Маньхуа"
            "light_novel" -> "Ранобэ"
            "novel" -> "Новелла"
            "one_shot" -> "Ван-шот"
            "doujin" -> "Додзинси"
            else -> str.uppercase()
        }
    }

    private fun decodeStatus(str: String): String {
        return when(str) {
            "anons" -> "Анонс"
            "ongoing" -> "Выпускается"
            "released" -> "Завершён"
            "paused" -> "Выпуск приостановлен"
            "discontinued" -> "Выпуск прекращён"
            else -> str
        }
    }
}