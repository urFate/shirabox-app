package live.shirabox.core.entity.relation

import androidx.room.Embedded
import androidx.room.Relation
import live.shirabox.core.entity.ContentEntity
import live.shirabox.core.entity.EpisodeEntity
import live.shirabox.core.entity.RelatedContentEntity

data class CollectedContent(
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
