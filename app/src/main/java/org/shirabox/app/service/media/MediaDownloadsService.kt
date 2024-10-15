package org.shirabox.app.service.media

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.shirabox.app.R
import org.shirabox.app.service.media.model.DownloadsListener
import org.shirabox.app.service.media.model.EnqueuedTask
import org.shirabox.app.service.media.model.MediaDownloadTask
import org.shirabox.core.db.AppDatabase
import kotlin.math.roundToInt

class MediaDownloadsService : Service() {

    companion object {
        private const val CHANNEL_ID = "SB_DOWNLOADS"
        private lateinit var db: AppDatabase

        private val job = SupervisorJob()
        private val scope = CoroutineScope(Dispatchers.IO + job)

        val helper = DownloadsServiceHelper(scope)
    }

    private lateinit var listener: DListener
    private var initStartId: Int? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("DOWNLOADS_D", "Service started.")

        db = AppDatabase.getAppDataBase(this@MediaDownloadsService)!!

        scope.launch {
            if (initStartId == null) {
                initNotification()
                helper.initQueryJob()
                initStartId = startId
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        println("Destroying service...")
        helper.removeListener(listener)
        initStartId = null
    }

    private fun initNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(notificationChannel(this))
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setContentTitle(getString(R.string.episodes_downloading))
            setSmallIcon(R.drawable.ic_stat_shirabox_notification)
            setPriority(NotificationCompat.PRIORITY_LOW)
        }

        manager.apply {
            builder.setProgress(100, 0, true)
            notify(1, builder.build())
        }

        listener = DListener(manager, builder, this)

        helper.addListener(listener)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun notificationChannel(context: Context) = NotificationChannel(
        CHANNEL_ID,
        context.getString(R.string.downloads_notification_channel),
        NotificationManager.IMPORTANCE_LOW
    ).apply {
        description = context.getString(R.string.downloads_notification_channel_desc)
        lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC

    }

    private class DListener(
        val manager: NotificationManager,
        val baseBuilder: NotificationCompat.Builder,
        val service: Service,
    ) : DownloadsListener {
        private val db = AppDatabase.getAppDataBase(service)!!
        private val job = SupervisorJob()
        private val scope = CoroutineScope(Dispatchers.IO + job)
        private var exception: Exception? = null

        private val notificationId = 1

        override fun onCurrentTaskChanged(task: EnqueuedTask) {
            val contentUid = task.mediaDownloadTask.contentUid
            val title = db.contentDao().getContentByUid(contentUid).name
            
            scope.launch {
                helper.getQueryByGroupId(task.mediaDownloadTask.contentUid, task.mediaDownloadTask.group).collect { list ->
                    val currentPosition = list?.indexOf(task)?.inc() ?: 0
                    val querySize = list?.size ?: 0

                    task.progressState.collect { progress ->
                        baseBuilder
                            .setContentTitle("$title (${task.mediaDownloadTask.group})")
                            .setContentText(service.getString(R.string.episodes_downloading_counter, currentPosition, querySize))
                            .setProgress(100, progress.times(100).roundToInt(), progress < 0.001F)

                        manager.notify(notificationId, baseBuilder.build())
                    }
                }
            }
        }

        override fun onTaskFinish(task: EnqueuedTask, exception: Exception?) {
            if (exception == null) {
                println("Task finished without errors.")
                val mediaDownloadTask = task.mediaDownloadTask

                scope.launch {
                    mediaDownloadTask.uid?.let {
                        writeDownloadPath(
                            uid = it,
                            task = mediaDownloadTask
                        )
                    }
                }
            }

            this.exception = if (exception !is DownloadsServiceHelper.ForcedInterruptionException) {
                exception
            } else this.exception
        }

        override fun onLifecycleEnd() {
            val finishNotificationId = System.currentTimeMillis().div(1000).toInt()

            baseBuilder
                .setProgress(0, 0, false)
                .apply {
                    if (exception == null) {
                        setContentText(service.getString(R.string.episodes_downloading_finished))
                    } else {
                        setContentText(service.getString(R.string.episodes_downloading_failed))
                    }
                }

            manager.cancel(notificationId)
            manager.notify(finishNotificationId, baseBuilder.build())

            service.stopSelf()
        }

        private fun writeDownloadPath(uid: Int, task: MediaDownloadTask) {
            val episode = db.episodeDao().getEpisodeByUid(uid)
            val offlinePath = episode.offlineVideos?.toMutableMap() ?: mutableMapOf()

            offlinePath[task.quality] = task.file
            println("Writing video path: ${offlinePath[task.quality]}")

            db.episodeDao().updateEpisodes(episode.copy(offlineVideos = offlinePath))
        }
    }
}