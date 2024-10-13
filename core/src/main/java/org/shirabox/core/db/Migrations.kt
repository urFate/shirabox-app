package org.shirabox.core.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.serialization.json.Json
import org.shirabox.core.model.ActingTeam
import org.shirabox.core.model.ContentKind
import org.shirabox.core.model.ReleaseStatus

object Migrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Initialize new columns
            db.execSQL("ALTER TABLE 'episode' ADD COLUMN 'acting_team_name' TEXT NOT NULL DEFAULT 'Unknown Team'")
            db.execSQL("ALTER TABLE 'episode' ADD COLUMN 'acting_team_icon' TEXT NULL")
            db.execSQL("ALTER TABLE 'episode' ADD COLUMN 'video_length' INTEGER NULL")
            db.execSQL("ALTER TABLE 'episode' ADD COLUMN 'view_timestamp' INTEGER NULL")

            val episodesCursor = db.query("SELECT * FROM 'episode'")
            val contentCursor = db.query("SELECT * FROM 'content'")

            // Divide [acting_team] column on two columns
            if(episodesCursor.moveToFirst()) {
                do {
                    val uidIndex = episodesCursor.getColumnIndex("uid")
                    val teamIndex = episodesCursor.getColumnIndex("acting_team")

                    if (uidIndex < 0) continue

                    val episodeUid = episodesCursor.getLong(uidIndex)

                    val actingTeam =
                        Json.decodeFromString<ActingTeam>(episodesCursor.getString(teamIndex))
                    val values = ContentValues().apply {
                        put("acting_team_name", actingTeam.name)
                        put("acting_team_icon", actingTeam.logoUrl)
                    }

                    db.update(
                        "episode", SQLiteDatabase.CONFLICT_REPLACE, values, "uid=?", arrayOf(episodeUid)
                    )
                } while (episodesCursor.moveToNext())
            }

            // Map content cyrillic values to enum classes
            if(contentCursor.moveToFirst()) {
                do {
                    val uidIndex = contentCursor.getColumnIndex("uid")
                    val statusIndex = contentCursor.getColumnIndex("status")
                    val kindIndex = contentCursor.getColumnIndex("kind")

                    if (uidIndex < 0) continue

                    val contentUid = contentCursor.getLong(uidIndex)
                    val oldStatus = contentCursor.getString(statusIndex)
                    val oldKind = contentCursor.getString(kindIndex)

                    val releaseStatus = when (oldStatus) {
                        "Анонс" -> ReleaseStatus.ANNOUNCED
                        "Выпускается" -> ReleaseStatus.RELEASING
                        "Завершён" -> ReleaseStatus.FINISHED
                        "Выпуск приостановлен" -> ReleaseStatus.PAUSED
                        "Выпуск прекращён" -> ReleaseStatus.DISCOUNTED
                        else -> ReleaseStatus.UNKNOWN
                    }

                    val contentKind = when(oldKind) {
                        "Сериал" -> ContentKind.TV
                        "Фильм" -> ContentKind.MOVIE
                        "Спешл" -> ContentKind.SPECIAL
                        "OVA" -> ContentKind.OVA
                        "ONA" -> ContentKind.ONA
                        else -> ContentKind.OTHER
                    }

                    val values = ContentValues().apply {
                        put("status", releaseStatus.toString())
                        put("kind", contentKind.toString())
                    }

                    db.update(
                        "content", SQLiteDatabase.CONFLICT_REPLACE, values, "uid=?", arrayOf(contentUid)
                    )
                } while (contentCursor.moveToNext())
            }

            db.execSQL("ALTER TABLE 'episode' DROP COLUMN 'acting_team'")
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Initialize new columns
            db.execSQL("ALTER TABLE 'content' ADD COLUMN 'episodes_notifications' INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE 'content' ADD COLUMN 'shirabox_id' INTEGER NULL")

            val contentCursor = db.query("SELECT * FROM 'content'")

            if(contentCursor.moveToFirst()) {
                do {
                    val uidIndex = contentCursor.getColumnIndex("uid")
                    val isFavouriteIndex = contentCursor.getColumnIndex("favourite")

                    if (uidIndex < 0) continue

                    val contentUid = contentCursor.getLong(uidIndex)
                    val isFavourite = contentCursor.getInt(isFavouriteIndex)

                    val values = ContentValues().apply {
                        put("episodes_notifications", isFavourite)
                    }

                    db.update(
                        "content", SQLiteDatabase.CONFLICT_REPLACE, values, "uid=?", arrayOf(contentUid)
                    )
                } while (contentCursor.moveToNext())
            }
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Initialize new columns
            db.execSQL("ALTER TABLE 'episode' ADD COLUMN 'offline_videos' TEXT NULL")
        }
    }
}