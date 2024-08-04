package org.shirabox.data.schedule.shirabox

import fuel.httpGet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.shirabox.core.model.ScheduleEntry
import org.shirabox.data.schedule.AbstractScheduleRepository
import java.text.SimpleDateFormat

object ShiraBoxScheduleRepository : AbstractScheduleRepository() {

    private const val API_ENDPOINT = "https://api.shirabox.org/v1"
    private const val IMAGE_HOST = "https://api.shirabox.org/assets"

    override suspend fun fetchSchedule(): Flow<List<ScheduleEntry>> = flow {
        val request = "$API_ENDPOINT/schedule/".httpGet().takeIf {
            it.statusCode == 200
        }?.body

        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

        request?.let {
            emit(json.decodeFromString<ScheduleList>(request).schedule.map {
                ScheduleEntry(
                    id = it.id,
                    name = it.name,
                    russianName = it.russianName,
                    image = IMAGE_HOST + it.image,
                    nextEpisodeNumber = it.nextEpisodeNumber,
                    released = it.released,
                    releaseRange = it.releaseRange.map { time -> parser.parse(time).time },
                    shikimoriId = it.shikimoriId
                )
            })
        } ?: emit(emptyList())
    }
}