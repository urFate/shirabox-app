package live.shirabox.core.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    @ColumnInfo(name = "content_shikimori_id") val contentShikimoriId: Int,
    @ColumnInfo(name = "receive_timestamp") val receiveTimestamp: Long,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "body") val body: String,
    @ColumnInfo(name = "thumbnail") val thumbnailUrl: String
)