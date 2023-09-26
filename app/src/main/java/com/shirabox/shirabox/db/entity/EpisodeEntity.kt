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
    @ColumnInfo(name = "videos") val videos: Map<Quality, String> = emptyMap(),
    @ColumnInfo(name = "chapters") val pages: List<String> = emptyList(),
    @ColumnInfo(name = "opening_markers") val videoMarkers: Pair<Long, Long> = -1L to -1L,
    @ColumnInfo(name = "reading_page") val readingPage: Int = -1,
    @ColumnInfo(name = "watching_time") val watchingTime: Long = -1L,
    @ColumnInfo(name = "type") val type: ContentType
)

