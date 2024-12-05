package org.shirabox.core.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.shirabox.core.model.Quality
import org.shirabox.core.model.StreamProtocol

@Entity(tableName = "download")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "uid") val uid: Long = 0,
    @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "file") val file: String,
    @ColumnInfo(name = "mpeg_bytes") val mpegBytes: Long,
    @ColumnInfo(name = "hls_fragment") val hlsFragment: Int?,
    @ColumnInfo(name = "paused_progress") val pausedProgress: Float,
    @ColumnInfo(name = "quality") val quality: Quality,
    @ColumnInfo(name = "stream_protocol") val streamProtocol: StreamProtocol,
    @ColumnInfo(name = "group") val group: String,
    @ColumnInfo(name = "content_uid") val contentUid: Long,
    @ColumnInfo(name = "episode_uid") val episodeUid: Int
)