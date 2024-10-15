package org.shirabox.core.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.shirabox.core.model.Quality
import org.shirabox.core.model.StreamProtocol

@Entity(tableName = "download")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    val url: String,
    val file: String,
    val pausedProgress: Float,
    val quality: Quality,
    val streamProtocol: StreamProtocol,
    val group: String,
    val contentUid: Long,
    val episodeUid: Int?
)