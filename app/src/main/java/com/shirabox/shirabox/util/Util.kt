package com.shirabox.shirabox.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.C
import com.google.accompanist.systemuicontroller.SystemUiController
import com.shirabox.shirabox.model.Content
import com.shirabox.shirabox.model.ContentType
import com.shirabox.shirabox.model.Rating
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

        val dummyContent = Content(
            name = "",
            altName = "",
            description = "",
            coverUri = "",
            production = "",
            releaseYear = "",
            type = ContentType.ANIME,
            kind = "",
            episodesCount = 0,
            rating = Rating(0.0),
            shikimoriID = 0,
            genres = emptyList()
        )

        /**
         * Fill contents list with dummies until loading finished.
         * Required for smooth placeholders transition
         */
        fun dummyContentsList(
            isReady: Boolean,
            amount: Int,
            contents: List<Content>
        ): List<Content> {
            return if (!isReady) {
                return buildList {
                    repeat(amount) {
                        add(dummyContent)
                    }
                }
            } else contents
        }

        @Composable
        fun maxElementsInRow(itemWidth: Int) : Int {
            val configuration = LocalConfiguration.current
            val screenWidth = configuration.screenWidthDp

            return (screenWidth / itemWidth).inc()
        }

        @Composable
        fun maxElementsInColumn(itemHeight: Int) : Int {
            val configuration = LocalConfiguration.current
            val screenHeight = configuration.screenHeightDp

            return (screenHeight / itemHeight).inc()
        }
    }
}