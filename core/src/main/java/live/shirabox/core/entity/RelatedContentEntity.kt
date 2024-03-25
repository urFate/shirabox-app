package live.shirabox.core.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "related")
data class RelatedContentEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "content_uid") val contentUid: Int,
    @ColumnInfo(name = "shikimori_id") val shikimoriID: Int,
)
