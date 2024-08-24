package org.shirabox.data.shirabox

import fuel.httpGet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import org.shirabox.core.model.ScheduleEntry
import org.shirabox.core.model.ShiraBoxAnime
import java.text.SimpleDateFormat
import java.util.Locale

object ShiraBoxRepository {

    private const val API_ENDPOINT = "https://api.shirabox.org/v1"
    private const val IMAGE_HOST = "https://api.shirabox.org/assets"

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true; encodeDefaults = true }
    private val dateParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ROOT)

    suspend fun fetchAnime(shikimoriId: Int): Flow<ShiraBoxAnime> = flow {
        val request = "$API_ENDPOINT/anime".httpGet(
            listOf(
                "shikimoriId" to shikimoriId.toString(),
            )
        ).takeIf {
            it.statusCode == 200
        }?.body

        request?.let {
            val rawAnime = json.decodeFromString<ShiraBoxAnimeResponse>(request).anime

            emit(
                ShiraBoxAnime(
                    id = rawAnime.id,
                    name = rawAnime.name,
                    russianName = rawAnime.russianName,
                    image = IMAGE_HOST + rawAnime.image,
                    schedule = ShiraBoxAnime.Schedule(
                        releaseRange = rawAnime.schedule.releaseRange.map { time -> dateParser.parse(time)?.time
                            ?: System.currentTimeMillis() },
                        released = rawAnime.schedule.released
                    ),
                    shikimoriId = rawAnime.shikimoriId
                )
            )
        }
    }

    suspend fun fetchSchedule(): Flow<List<ScheduleEntry>> = flow {
        val request = "$API_ENDPOINT/schedule/".httpGet().takeIf {
            it.statusCode == 200
        }?.body

        request?.let {
            emit(json.decodeFromString<ScheduleList>(request).schedule.map {
                ScheduleEntry(
                    id = it.id,
                    name = it.name,
                    russianName = it.russianName,
                    image = IMAGE_HOST + it.image,
                    nextEpisodeNumber = it.nextEpisodeNumber,
                    released = it.released,
                    releaseRange = it.releaseRange.map { time -> dateParser.parse(time).time },
                    shikimoriId = it.shikimoriId
                )
            })
        } ?: emit(emptyList())
    }
}