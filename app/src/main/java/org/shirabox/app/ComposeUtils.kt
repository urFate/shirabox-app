package org.shirabox.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

object ComposeUtils {
    @Composable
    fun getWeekDayTitle(day: Int) = when(day) {
        1 -> stringResource(id = R.string.schedule_sunday)
        2 -> stringResource(id = R.string.schedule_monday)
        3 -> stringResource(id = R.string.schedule_tuesday)
        4 -> stringResource(id = R.string.schedule_wednesday)
        5 -> stringResource(id = R.string.schedule_thursday)
        6 -> stringResource(id = R.string.schedule_friday)
        7 -> stringResource(id = R.string.schedule_saturday)
        else -> "null"
    }
}