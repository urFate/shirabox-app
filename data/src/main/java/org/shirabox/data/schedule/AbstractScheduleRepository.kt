package org.shirabox.data.schedule

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import org.shirabox.core.model.ScheduleEntry

abstract class AbstractScheduleRepository {

    protected val json = Json { ignoreUnknownKeys = true; coerceInputValues = true; encodeDefaults = true }

    abstract suspend fun fetchSchedule(): Flow<List<ScheduleEntry>>
}