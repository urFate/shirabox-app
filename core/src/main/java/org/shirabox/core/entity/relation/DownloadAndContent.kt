package org.shirabox.core.entity.relation

import androidx.room.Embedded
import androidx.room.Relation
import org.shirabox.core.entity.ContentEntity
import org.shirabox.core.entity.DownloadEntity

data class DownloadAndContent(
    @Embedded val downloadEntity: DownloadEntity,

    @Relation(
        parentColumn = "content_uid",
        entityColumn = "uid"
    )
    val contentEntity: ContentEntity
)