package org.shirabox.app

import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.shirabox.core.db.AppDatabase

@HiltAndroidApp
class App : Application(), ImageLoaderFactory {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .build()
    }

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        val db = AppDatabase.getAppDataBase(this)!!

        // Subscribe to notifications
        scope.launch {
            db.contentDao().getFavourites().collect { list ->
                list.forEach { favouriteAnime ->
                    if (favouriteAnime.shiraboxId != null) {
                        val topic = "id-${favouriteAnime.shiraboxId}"

                        if (favouriteAnime.episodesNotifications) {
                            Firebase.messaging.subscribeToTopic(topic)
                        } else {
                            Firebase.messaging.unsubscribeFromTopic(topic)
                        }
                    }
                }

                Log.i("ShiraBoxApplication", "Episodes topics subscription finished.")
            }
        }
    }
}
