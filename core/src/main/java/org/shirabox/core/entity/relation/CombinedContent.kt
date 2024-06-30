package org.shirabox.core.entity.relation

import androidx.room.Embedded
import androidx.room.Relation
import org.shirabox.core.entity.ContentEntity
import org.shirabox.core.entity.EpisodeEntity

data class CombinedContent(
    @Embedded val content: ContentEntity,

    @Relation(
        parentColumn = "uid",
        entityColumn = "content_uid"
    )
    val episodes: List<EpisodeEntity>,
)
