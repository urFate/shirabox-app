package org.shirabox.core.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.shirabox.core.db.dao.ContentDao
import org.shirabox.core.db.dao.EpisodeDao
import org.shirabox.core.db.dao.NotificationDao
import org.shirabox.core.entity.ContentEntity
import org.shirabox.core.entity.EpisodeEntity
import org.shirabox.core.entity.NotificationEntity
import org.shirabox.core.util.Values

@Database(
    entities = [ContentEntity::class, EpisodeEntity::class, NotificationEntity::class],
    version = 2
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contentDao(): ContentDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getAppDataBase(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class) {
                    INSTANCE = Room
                        .databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            Values.DATABASE_NAME
                        )
                        .addMigrations(Migrations.MIGRATION_1_2)
                        .build()
                }
            }
            return INSTANCE
        }
    }
}