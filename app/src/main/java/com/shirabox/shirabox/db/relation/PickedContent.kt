package com.shirabox.shirabox.db.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.shirabox.shirabox.db.entity.ContentEntity
import com.shirabox.shirabox.db.entity.EpisodeEntity
import com.shirabox.shirabox.db.entity.RelatedContentEntity

data class PickedContent(
    @Embedded val content: ContentEntity,

    @Relation(
        parentColumn = "uid",
        entityColumn = "content_uid"
    )
    val episodes: List<EpisodeEntity>,

    @Relation(
        parentColumn = "uid",
        entityColumn = "content_uid"
    )
    val related: List<RelatedContentEntity>
)
