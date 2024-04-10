package live.shirabox.shirabox

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import live.shirabox.core.entity.NotificationEntity
import live.shirabox.core.model.ContentType
import live.shirabox.core.util.Util
import live.shirabox.shirabox.db.AppDatabase
import live.shirabox.shirabox.ui.activity.resource.ResourceActivity
import java.net.URL

class NotificationService : FirebaseMessagingService() {
    companion object {
        private const val TAG = "ShiraBoxService"
        private const val MAIN_CHANNEL_ID = "SB_NOTIFICATIONS"
        lateinit var db: AppDatabase
    }

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private data class MessageData(
        val title: String,
        val body: String,
        val thumbnailUrl: String?,
        val shikimoriId: Int
    )

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New token observed: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        remoteMessage.data.ifEmpty { return }

        Log.d(TAG, "Message data payload: ${remoteMessage.data}")

        try {
            db = AppDatabase.getAppDataBase(this)!!

            val data = remoteMessage.data

            val messageData = MessageData(
                title = data["title"] ?: "Undefined title",
                body = data["body"] ?: "Undefined notification body",
                shikimoriId = data["shikimori_id"]!!.toInt(),
                thumbnailUrl = data["thumbnail"]
            )

            scope.launch {
                launch { sendNotification(messageData) }
                launch { saveNotification(messageData) }
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private suspend fun sendNotification(messageData: MessageData) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(notificationChannel(this))
        }

        val thumbnailBitmap = messageData.thumbnailUrl?.let {
            withContext(Dispatchers.IO) {
                async { Util.getBitmapFromURL(URL(it)) }
            }.await()
        }

        val activityIntent = Intent(this, ResourceActivity::class.java).apply {
            putExtra("id", messageData.shikimoriId)
            putExtra("type", ContentType.ANIME.toString())
        }
        val activityPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(activityIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val notification: Notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this@NotificationService, MAIN_CHANNEL_ID).apply {
                setContentTitle(messageData.title)
                setContentText(messageData.body)
                setLargeIcon(thumbnailBitmap)
                setSmallIcon(R.drawable.ic_stat_shirabox_notification)
                setContentIntent(activityPendingIntent)
                setAutoCancel(true)
            }.build()
        } else {
            Notification.Builder(this@NotificationService).apply {
                setContentTitle(messageData.title)
                setContentText(messageData.body)
                setLargeIcon(thumbnailBitmap)
                setSmallIcon(R.drawable.ic_stat_shirabox_notification)
                setContentIntent(activityPendingIntent)
                setAutoCancel(true)
            }.build()
        }

        manager.notify(System.nanoTime().toInt(), notification)
    }

    private suspend fun saveNotification(messageData: MessageData) {
        withContext(Dispatchers.IO) {
            db.notificationDao().insertNotification(
                NotificationEntity(
                    contentShikimoriId = messageData.shikimoriId,
                    receiveTimestamp = System.currentTimeMillis(),
                    title = messageData.title,
                    body = messageData.body,
                    thumbnailUrl = messageData.thumbnailUrl ?: ""
                )
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun notificationChannel(context: Context) = NotificationChannel(
        MAIN_CHANNEL_ID,
        context.getString(R.string.notificaion_channel),
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply { description = context.getString(R.string.notificaion_channel_description) }
}