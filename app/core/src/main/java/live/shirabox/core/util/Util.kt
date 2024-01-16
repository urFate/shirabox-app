package live.shirabox.core.util

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.text.Html
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.C
import com.google.accompanist.systemuicontroller.SystemUiController
import live.shirabox.core.entity.ContentEntity
import live.shirabox.core.model.Content
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

        fun maxElementsInRow(itemWidth: Int, configuration: Configuration): Int {
            val screenWidth = configuration.screenWidthDp

            return (screenWidth / itemWidth).inc()
        }

        fun calcGridHeight(itemsCount: Int, itemHeight: Int, columns: Int): Int {
            return itemHeight.plus(16)
                .times(itemsCount.floorDiv(columns).plus(itemsCount.mod(columns)))
        }

        fun decodeHtml(str: String): String {
            return when {
                Build.VERSION.SDK_INT >= 24 -> Html.fromHtml(str, Html.FROM_HTML_MODE_LEGACY)
                    .toString()

                else -> Html.fromHtml(str).toString()
            }
        }

        fun mapEntityToContent(contentEntity: ContentEntity): Content {
            return Content(
                name = contentEntity.name,
                altName = contentEntity.altName,
                description = contentEntity.description,
                image = contentEntity.image,
                production = contentEntity.production,
                releaseYear = contentEntity.releaseYear,
                type = contentEntity.type,
                kind = contentEntity.kind,
                status = contentEntity.status,
                episodes = contentEntity.episodes,
                episodesAired = contentEntity.episodesAired,
                episodeDuration = contentEntity.episodeDuration,
                rating = contentEntity.rating,
                shikimoriID = contentEntity.shikimoriID,
                genres = contentEntity.genres
            )
        }

        fun mapContentToEntity(
            content: Content,
            isFavourite: Boolean,
            lastViewTimestamp: Long,
            pinnedSources: List<String>
        ): ContentEntity {
            return ContentEntity(
                name = content.name,
                altName = content.altName,
                description = content.description,
                image = content.image,
                production = content.production,
                releaseYear = content.releaseYear,
                type = content.type,
                kind = content.kind,
                status = content.status,
                episodes = content.episodes,
                episodesAired = content.episodesAired,
                episodeDuration = content.episodeDuration,
                rating = content.rating,
                shikimoriID = content.shikimoriID,
                code = encodeString(content.altName),
                genres = content.genres,
                isFavourite = isFavourite,
                lastViewTimestamp = lastViewTimestamp,
                pinnedSources = pinnedSources
            )
        }

        inline fun <VM : ViewModel> viewModelFactory(crossinline f: () -> VM) =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T = f() as T
            }

        fun encodeString(str: String): String {
            return str.replace(Regex("[^a-zA-Z_0-9]"), "-")
                .replace(Regex("(^[A-Z][A-Z]^[A-Z])"), "-$1")
                .replace(Regex("^-"), "").replace(Regex("-$"), "")
                .replace(Regex("([a-zA-Z])([0-9])"), "$1-$2")
                .replace(Regex("([0-9])([a-zA-Z]^[nd])"), "$1-$2")
                .replace(Regex("-{2,}"), "-")
                .lowercase()
        }

        fun getAppVersion(
            context: Context,
        ): String? {
            return try {
                val packageManager = context.packageManager
                val packageName = context.packageName
                val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
                } else {
                    packageManager.getPackageInfo(packageName, 0)
                }

                packageInfo.versionName
            } catch (e: Exception) {
                null
            }
        }
    }
}