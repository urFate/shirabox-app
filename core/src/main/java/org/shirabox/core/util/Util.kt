package org.shirabox.core.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.text.Html
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.C
import com.google.accompanist.systemuicontroller.SystemUiController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.shirabox.core.entity.ContentEntity
import org.shirabox.core.model.Content
import org.shirabox.core.serializable.Topic
import java.net.URL
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
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
            return Html.fromHtml(str, Html.FROM_HTML_MODE_LEGACY)
                .toString()
        }

        fun mapEntityToContent(contentEntity: ContentEntity): Content {
            return Content(
                name = contentEntity.name,
                enName = contentEntity.enName,
                altNames = contentEntity.altNames,
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
                shiraboxId = contentEntity.shiraboxId,
                shikimoriId = contentEntity.shikimoriID,
                genres = contentEntity.genres
            )
        }

        fun mapContentToEntity(
            contentUid: Long? = null,
            content: Content,
            isFavourite: Boolean,
            episodesNotifications: Boolean,
            lastViewTimestamp: Long,
            pinnedTeams: List<String>
        ): ContentEntity {
            val entity = ContentEntity(
                name = content.name,
                enName = content.enName,
                altNames = content.altNames,
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
                shiraboxId = content.shiraboxId,
                shikimoriID = content.shikimoriId,
                genres = content.genres,
                isFavourite = isFavourite,
                episodesNotifications = episodesNotifications,
                lastViewTimestamp = lastViewTimestamp,
                pinnedTeams = pinnedTeams
            )

            return when(contentUid) {
                null -> entity
                else -> entity.copy(uid = contentUid)
            }
        }

        @OptIn(ExperimentalEncodingApi::class)
        fun encodeTopic(repository: String, actingTeam: String, contentEnName: String): String {
            val json = Json.encodeToString(
                Topic(
                    repository = repository,
                    actingTeam = actingTeam,
                    md5 = contentEnName.md5()
                )
            )

            return Base64.encode(json.toByteArray())
        }
        suspend fun getBitmapFromURL(url: URL): Bitmap? {
            return try {
                val inputStream = withContext(Dispatchers.IO) {
                    async {
                        val connection = url.openConnection()
                        connection.setDoInput(true)
                        connection.connect()
                        connection.inputStream
                    }
                }

                BitmapFactory.decodeStream(inputStream.await())
            } catch (_: Exception) { null }
        }

        fun formatBadgeNumber(number: Int): String {
            return if (number < 9) number.toString() else "9+"
        }

        fun openUri(context: Context, uri: Uri) {
            ContextCompat.startActivity(
                context,
                Intent(Intent.ACTION_VIEW).setData(uri),
                null
            )
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
            } catch (_: Exception) {
                null
            }
        }

        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            // For 29 api or above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val capabilities =
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                        ?: return false
                return when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    else -> false
                }
            }
            // For below 29 api
            else {
                if (connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!.isConnectedOrConnecting) {
                    return true
                }
            }
            return false
        }

        fun startForegroundService(context: Context, clazz: Class<*>) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(Intent(context, clazz))
            } else {
                context.startService(Intent(context, clazz))
            }
        }
    }
}