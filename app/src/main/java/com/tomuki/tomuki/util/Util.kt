package com.tomuki.tomuki.util

import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.C
import com.google.accompanist.systemuicontroller.SystemUiController
import kotlin.time.Duration.Companion.milliseconds

class Util {
    companion object {
        fun hideSystemUi(controller: SystemUiController) {
            controller.isSystemBarsVisible = false
            controller.systemBarsBehavior = WindowInsetsControllerCompat
                .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        fun formatMilliseconds(timeMs: Long): String {
            val time = if (timeMs == C.TIME_UNSET) 0 else timeMs

            return time.milliseconds.toComponents { hours, minutes, seconds, _ ->
                "%s%02d:%02d".format(
                    if (hours != 0L) "%02d:".format(hours) else "",
                    minutes,
                    seconds
                )
            }
        }
    }
}