package org.shirabox.data.catalog.shikimori

import fuel.httpGet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import okhttp3.Request
import org.shirabox.core.model.Content
import org.shirabox.core.model.ContentKind
import org.shirabox.core.model.ContentType
import org.shirabox.core.model.Rating
import org.shirabox.core.model.ReleaseStatus
import org.shirabox.core.util.Util
import java.net.SocketTimeoutException
import java.util.Calendar


object ShikimoriRepository : AbstractCatalogRepository("Shikimori", "https://shikimori.one") {

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true; encodeDefaults = true }

    override fun fetchOngoings(page: Int, type: ContentType): Flow<List<Content>> = flow {
        val list = when (type) {
            ContentType.ANIME -> fetchCatalogContent(
                "animes", page, listOf(
                    "status" to "ongoing", "order" to "ranked", "season" to currentSeason()
                )
            )

            ContentType.MANGA -> fetchCatalogContent(
                "mangas", page, listOf(
                    "status" to "ongoing", "order" to "ranked", "season" to currentSeason()
                )
            )

            ContentType.RANOBE -> fetchCatalogContent(
                "ranobe", page, listOf(
                    "status" to "ongoing", "order" to "ranked", "season" to currentSeason()
                )
            )
        }

        emit(list)
    }

    override fun fetchPopulars(pages: IntRange, type: ContentType): Flow<List<Content>> = flow {
        val list = when (type) {
            ContentType.ANIME -> {
                pages.map {
                    fetchCatalogContent("animes", it, listOf(
                            "order" to "popularity"
                        ))
                }.flatten()
            }

            ContentType.MANGA -> {
                pages.map {
                    fetchCatalogContent("mangas", it, listOf(
                            "order" to "popularity"
                        ))
                }.flatten()
            }

            ContentType.RANOBE -> {
                pages.map {
                    fetchCatalogContent("ranobe", it, listOf(
                            "order" to "popularity"
                        ))
                }.flatten()
            }
        }

        emit(list)
    }


    override fun fetchContent(id: Int, type: ContentType): Flow<Content> = flow {
        val request: Request = Request.Builder().apply {
            url("$url/api/${sectionFromType(type)}/$id")
        }.build()

        try {
            val response = myClient.newCall(request).execute().body.string()

            val content = when (type) {
                ContentType.ANIME -> {
                    val data = json.decodeFromString<AnimeData>(response)

                    Content(
                        name = data.russian,
                        enName = data.name,
                        altNames = data.synonyms,
                        description = Util.decodeHtml(data.description.toString()),
                        image = "$url/${data.image.original}",
                        production = data.studios.firstOrNull()?.name,
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
                        shikimoriId = data.id,
                        genres = data.genres.map { it.russian }
                    )
                }

                ContentType.MANGA, ContentType.RANOBE -> {
                    val data = json.decodeFromString<BookData>(response)

                    Content(
                        name = data.russian,
                        enName = data.name,
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
                        shikimoriId = data.id,
                        genres = data.genres.map { it.russian}
                    )
                }
            }

            emit(content)
        } catch (ex: Exception) { throw ex } catch (ex: SocketTimeoutException) { throw ex }
    }

    override fun search(query: String, type: ContentType): Flow<List<Content>> = flow {
        val response = "$url/api/${sectionFromType(type)}".httpGet(
            listOf(
                "search" to query,
                "limit" to "50"
            )
        ).body

        val list = when (type) {
            ContentType.ANIME -> {
                val data = json.decodeFromString<List<LibraryAnimeData>>(response)

                data.filter { it.kind != "music" }.map {
                    Content(
                        name = it.russian,
                        enName = it.name,
                        image = "$url/${it.image.original}",
                        releaseYear = it.airedOn?.substring(0..3) ?: "2001",
                        type = type,
                        kind = decodeKind(it.kind.toString()),
                        status = decodeStatus(it.status),
                        episodes = it.episodes,
                        episodesAired = it.episodesAired,
                        rating = Rating(it.score.toDouble()),
                        shikimoriId = it.id
                    )
                }
            }

            ContentType.MANGA, ContentType.RANOBE -> {
                val data = json.decodeFromString<List<LibraryBookData>>(response)

                data.filter { it.kind != "music" }.map {
                    Content(
                        name = it.russian,
                        enName = it.name,
                        image = "$url/${it.image.original}",
                        releaseYear = it.airedOn.substring(0..3),
                        type = type,
                        kind = decodeKind(it.kind),
                        status = decodeStatus(it.status),
                        episodes = it.chapters,
                        rating = Rating(it.score.toDouble()),
                        shikimoriId = it.id
                    )
                }
            }
        }

        emit(list)
    }

    override fun fetchRelated(id: Int, type: ContentType): Flow<List<Content>> = flow {
        val request: Request = Request.Builder().apply {
            url("$url/api/${sectionFromType(type)}/$id/related")
        }.build()

        try {
            val response = myClient.newCall(request).execute().body.string()
            val data = json.decodeFromString<List<RelatedItem>>(response)

            val contents = data.filter { it.relation != "Other" }.mapNotNull {
                when {
                    it.anime != null -> Content(
                        name = it.anime.russian,
                        enName = it.anime.name,
                        image = "$url/${it.anime.image.original}",
                        releaseYear = it.anime.releasedOn,
                        type = ContentType.ANIME,
                        kind = decodeKind(it.anime.kind.toString()),
                        status = decodeStatus(it.anime.status),
                        episodes = it.anime.episodes,
                        episodesAired = it.anime.episodesAired,
                        rating = Rating(average = it.anime.score.toDouble(), scores = mapOf()),
                        shikimoriId = it.anime.id
                    )

                    it.manga != null -> Content(
                        name = it.manga.russian,
                        enName = it.manga.name,
                        image = "$url/${it.manga.image.original}",
                        releaseYear = it.manga.releasedOn ?: "",
                        type = ContentType.MANGA,
                        kind = decodeKind(it.manga.kind.toString()),
                        status = decodeStatus(it.manga.status),
                        episodes = it.manga.chapters,
                        rating = Rating(average = it.manga.score.toDouble(), scores = mapOf()),
                        shikimoriId = it.manga.id
                    )

                    else -> null
                }
            }

            emit(contents)
        } catch (ex: Exception) { throw ex}
    }

    private suspend fun fetchCatalogContent(
        section: String, page: Int, query: List<Pair<String, String>>
    ): List<Content> {

        val response = "$url/api/$section".httpGet(
            listOf(
                "limit" to "16",
                "page" to "$page",
                *query.toTypedArray()
            )
        ).body

        return try {
            when (section) {
                "animes" -> {
                    val data = json.decodeFromString<List<LibraryAnimeData>>(response)

                    data.map {
                        Content(
                            name = it.russian,
                            enName = it.name,
                            image = "$url/${it.image.original}",
                            releaseYear = it.airedOn?.substring(0..3) ?: "2001",
                            type = ContentType.ANIME,
                            kind = decodeKind(it.kind.toString()),
                            status = decodeStatus(it.status),
                            episodes = it.episodes,
                            episodesAired = it.episodesAired,
                            rating = Rating(it.score.toDouble()),
                            shikimoriId = it.id
                        )
                    }
                }

                "mangas", "ranobe" -> {
                    val data = json.decodeFromString<List<LibraryBookData>>(response)

                    data.map {
                        Content(
                            name = it.russian,
                            enName = it.name,
                            image = "$url/${it.image.original}",
                            releaseYear = it.airedOn.substring(0..3),
                            type = ContentType.fromString(section),
                            kind = decodeKind(it.kind),
                            status = decodeStatus(it.status),
                            episodes = it.chapters,
                            rating = Rating(it.score.toDouble()),
                            shikimoriId = it.id
                        )
                    }
                }

                else -> emptyList()
            }
        } catch (_: Exception) { emptyList() }
    }

    private fun sectionFromType(contentType: ContentType): String {
        return when (contentType) {
            ContentType.MANGA -> "mangas"
            ContentType.RANOBE -> "ranobe"
            ContentType.ANIME -> "animes"
        }
    }

    private fun decodeKind(str: String?): ContentKind = when(str) {
        "tv" -> ContentKind.TV
        "movie" -> ContentKind.MOVIE
        "special", "tv_special" -> ContentKind.SPECIAL
        "ova" -> ContentKind.OVA
        "ona" -> ContentKind.ONA
        else -> ContentKind.OTHER
    }

    private fun decodeStatus(str: String): ReleaseStatus = when(str) {
        "anons" -> ReleaseStatus.ANNOUNCED
        "ongoing" -> ReleaseStatus.RELEASING
        "released" -> ReleaseStatus.FINISHED
        "paused" -> ReleaseStatus.PAUSED
        "discontinued" -> ReleaseStatus.DISCOUNTED
        else -> ReleaseStatus.UNKNOWN
    }

    private fun currentSeason(): String {
        val calendar = Calendar.getInstance()

        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)

        return when (dayOfYear) {
            in 1 until 100 -> "winter_$year"
            in 100 until 186 -> "spring_$year"
            in 186 until 279 -> "summer_$year"
            else -> "fall_$year"
        }
    }
}