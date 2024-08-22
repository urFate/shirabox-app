package org.shirabox.app

import android.app.Activity
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun bottomSheetDynamicNavColor(state: SheetState) {
        val window = (LocalContext.current as Activity).window
        val containerColor = BottomSheetDefaults.ContainerColor.toArgb()

        LaunchedEffect(state.hasExpandedState, state.isVisible) {
            if(state.hasExpandedState) {
                window.navigationBarColor = containerColor
            }
            if(!state.hasExpandedState || !state.isVisible) {
                window.navigationBarColor = Color.Transparent.toArgb()
            }
        }
    }
}