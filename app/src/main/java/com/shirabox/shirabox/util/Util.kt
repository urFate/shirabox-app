package com.shirabox.shirabox.util

import android.content.res.Configuration
import android.os.Build
import android.text.Html
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.C
import com.google.accompanist.systemuicontroller.SystemUiController
import kotlin.math.roundToInt
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

        fun maxElementsInRow(itemWidth: Int, configuration: Configuration) : Int {
            val screenWidth = configuration.screenWidthDp

            return (screenWidth / itemWidth).inc()
        }

        fun maxElementsInVerticalGrid(
            gridWidth: Int,
            itemWidth: Int,
            spacing: Int,
            density: Density
        ): Int {
            val availableWidth = with(density) { gridWidth.dp.toPx() - (spacing * (gridWidth - 1)) }
            return (availableWidth / itemWidth).roundToInt()
        }

        fun decodeHtml(str: String): String {
            return when {
                Build.VERSION.SDK_INT >= 24 -> Html.fromHtml(str, Html.FROM_HTML_MODE_LEGACY)
                    .toString()

                else -> Html.fromHtml(str).toString()
            }
        }

        inline fun <VM : ViewModel> viewModelFactory(crossinline f: () -> VM) =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>):T = f() as T
            }
    }
}