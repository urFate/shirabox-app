package live.shirabox.core.entity.relation

import androidx.room.Embedded
import androidx.room.Relation
import live.shirabox.core.entity.ContentEntity
import live.shirabox.core.entity.NotificationEntity

data class NotificationAndContent (
    @Embedded
    val notificationEntity: NotificationEntity,
    @Relation(
        parentColumn = "content_code",
        entityColumn = "code"
    )
    val contentEntity: ContentEntity
)