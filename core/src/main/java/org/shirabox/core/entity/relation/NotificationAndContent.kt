package org.shirabox.core.entity.relation

import androidx.room.Embedded
import androidx.room.Relation
import org.shirabox.core.entity.ContentEntity
import org.shirabox.core.entity.NotificationEntity

data class NotificationAndContent (
    @Embedded val notificationEntity: NotificationEntity,

    @Relation(
        parentColumn = "content_shikimori_id",
        entityColumn = "shikimori_id"
    )
    val contentEntity: ContentEntity
)