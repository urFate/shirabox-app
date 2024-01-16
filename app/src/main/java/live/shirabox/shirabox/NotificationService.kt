package live.shirabox.shirabox

import android.Manifest
import android.app.Notification
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.room.Room.databaseBuilder
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import live.shirabox.core.entity.NotificationEntity
import live.shirabox.shirabox.db.AppDatabase


class NotificationService : FirebaseMessagingService() {
    private val mainChannelId = "SB_NOTIFICATIONS"
    private val databaseName = "shirabox_db"

    private lateinit var appDatabase: AppDatabase

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("ShiraBoxService", "Refreshed token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        appDatabase = databaseBuilder(
            applicationContext,
            AppDatabase::class.java, databaseName
        ).build()

        val data = message.data

        message.notification?.let { remoteNotification ->
            val title = remoteNotification.title ?: "Undefined title"
            val body = remoteNotification.body?: "Undefined notification body"

            val notification: Notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder(this, mainChannelId).apply {
                    setContentTitle(title)
                    setContentText("ПЭЙЛОАД Payload: ${data.size}")
                    setSmallIcon(R.drawable.ic_stat_shirabox_notification)
                    setAutoCancel(true)
                }.build()
            } else {
                Notification.Builder(this).apply {
                    setContentTitle(title)
                    setContentText("ПЭЙЛОАД Payload: $data")
                    setSmallIcon(R.drawable.ic_stat_shirabox_notification)
                    setAutoCancel(true)
                }.build()
            }

            if (ActivityCompat
                .checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED
            ) NotificationManagerCompat.from(this).notify(1, notification)

            // Save notification into the database

            Log.d("ShiraBoxService","NOTIFICATION_CODE: ${data["code"]}")

            data["code"]?.let {
                scope.launch(Dispatchers.IO) {
                    val appDatabase = AppDatabase.getAppDataBase(this@NotificationService)!!
                    Log.d("ShiraBoxService", "WE ADDING IT TO THE DATABASE")

                    appDatabase.notificationDao().insertNotification(NotificationEntity(
                        contentCode = it,
                        receiveTimestamp = System.currentTimeMillis(),
                        text = body
                    ))
                }
            }
        }
    }
}