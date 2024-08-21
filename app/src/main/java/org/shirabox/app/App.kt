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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.shirabox.core.db.AppDatabase
import org.shirabox.data.shirabox.ShiraBoxRepository

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

        // Migrate notifications
        scope.launch {
            db.contentDao().getFavourites().collect { list ->
                list.forEach { favouriteAnime ->
                    val anime = ShiraBoxRepository.fetchAnime(favouriteAnime.shikimoriID)

                    anime.catch { return@catch }.collectLatest {
                        val topic = "id-${it.id}"
                        Firebase.messaging.subscribeToTopic(topic)
                    }
                }

                Log.i("ShiraBoxApplication", "Favourites topics subscription finished.")
            }
        }
    }
}
