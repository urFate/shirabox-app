package live.shirabox.core.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "content_enName") val contentEnName: String,
    @ColumnInfo(name = "receive_timestamp") val receiveTimestamp: Long,
    @ColumnInfo(name = "text") val text: String
)