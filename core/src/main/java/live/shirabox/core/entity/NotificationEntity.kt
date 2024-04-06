package live.shirabox.core.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "content_code") val contentCode: String,
    @ColumnInfo(name = "receive_timestamp") val receiveTimestamp: Long,
    @ColumnInfo(name = "text") val text: String
)