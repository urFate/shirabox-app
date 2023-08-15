package com.shirabox.shirabox.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shirabox.shirabox.db.dao.ContentDao
import com.shirabox.shirabox.db.dao.EpisodeDao
import com.shirabox.shirabox.db.dao.RelatedDao
import com.shirabox.shirabox.db.entity.ContentEntity
import com.shirabox.shirabox.db.entity.EpisodeEntity
import com.shirabox.shirabox.db.entity.RelatedContentEntity

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