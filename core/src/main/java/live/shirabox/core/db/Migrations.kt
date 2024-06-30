package live.shirabox.core.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.serialization.json.Json
import live.shirabox.core.model.ActingTeam
import live.shirabox.core.model.ReleaseStatus

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

            // Map content cyrillic status to enum values
            if(contentCursor.moveToFirst()) {
                do {
                    val uidIndex = contentCursor.getColumnIndex("uid")
                    val statusIndex = contentCursor.getColumnIndex("status")

                    if (uidIndex < 0) continue

                    val contentUid = contentCursor.getLong(uidIndex)
                    val oldStatus = contentCursor.getString(statusIndex)

                    val releaseStatus = when (oldStatus) {
                        "Анонс" -> ReleaseStatus.ANNOUNCED
                        "Выпускается" -> ReleaseStatus.RELEASING
                        "Завершён" -> ReleaseStatus.FINISHED
                        "Выпуск приостановлен" -> ReleaseStatus.PAUSED
                        "Выпуск прекращён" -> ReleaseStatus.DISCOUNTED
                        else -> ReleaseStatus.UNKNOWN
                    }

                    val values = ContentValues().apply {
                        put("status", releaseStatus.toString())
                    }

                    db.update(
                        "content", SQLiteDatabase.CONFLICT_REPLACE, values, "uid=?", arrayOf(contentUid)
                    )
                } while (contentCursor.moveToNext())
            }

            db.execSQL("ALTER TABLE 'episode' DROP COLUMN 'acting_team'")
        }
    }
}