package org.shirabox.core.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.shirabox.core.model.ContentType
import org.shirabox.core.model.Quality

@Entity(tableName = "episode")
data class EpisodeEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int? = 0,
    @ColumnInfo(name = "content_uid") val contentUid: Long = -1,
    @ColumnInfo(name = "source") val source: String,
    @ColumnInfo(name = "acting_team_name", defaultValue = "Unknown Team") val actingTeamName: String,
    @ColumnInfo(name = "acting_team_icon") val actingTeamIcon: String? = null,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "episode") val episode: Int,
    @ColumnInfo(name = "upload_timestamp") val uploadTimestamp: Long,
    @ColumnInfo(name = "videos") val videos: Map<Quality, String> = emptyMap(),
    @ColumnInfo(name = "chapters") val pages: List<String> = emptyList(),
    @ColumnInfo(name = "opening_markers") val videoMarkers: Pair<Long, Long> = -1L to -1L,
    @ColumnInfo(name = "reading_page") val readingPage: Int = -1,
    @ColumnInfo(name = "watching_time") val watchingTime: Long = -1L,
    @ColumnInfo(name = "video_length") val videoLength: Long? = null,
    @ColumnInfo(name = "view_timestamp") val viewTimestamp: Long? = null,
    @ColumnInfo(name = "type") val type: ContentType
)

