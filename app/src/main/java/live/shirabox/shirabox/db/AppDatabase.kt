package live.shirabox.shirabox.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import live.shirabox.core.entity.ContentEntity
import live.shirabox.core.entity.EpisodeEntity
import live.shirabox.core.entity.RelatedContentEntity
import live.shirabox.shirabox.db.dao.ContentDao
import live.shirabox.shirabox.db.dao.EpisodeDao
import live.shirabox.shirabox.db.dao.RelatedDao

@Database(
    entities = [ContentEntity::class, EpisodeEntity::class, RelatedContentEntity::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contentDao(): ContentDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun relatedDao(): RelatedDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getAppDataBase(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "shirabox_db"
                    ).build()
                }
            }
            return INSTANCE
        }
    }
}