package com.shirabox.shirabox.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shirabox.shirabox.model.ContentType
import com.shirabox.shirabox.model.Quality

@Entity(tableName = "episode")
data class EpisodeEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "content_uid") val contentUid: Int = -1,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "episode") val episode: Int,
    @ColumnInfo(name = "upload_timestamp") val uploadTimestamp: Long,
    @ColumnInfo(name = "videos") val videos: Map<Quality, String>? = null,
    @ColumnInfo(name = "chapters") val pages: List<String>? = null,
    @ColumnInfo(name = "video_markers") val videoMarkers: Pair<Long?, Long?>? = null,
    @ColumnInfo(name = "reading_page") val readingPage: Int? = null,
    @ColumnInfo(name = "watching_time") val watchingTime: Long? = null,
    @ColumnInfo(name = "type") val type: ContentType
)

