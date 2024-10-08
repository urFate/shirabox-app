package org.shirabox.core.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.shirabox.core.model.ContentKind
import org.shirabox.core.model.ContentType
import org.shirabox.core.model.Rating
import org.shirabox.core.model.ReleaseStatus

@Entity(tableName = "content")
data class ContentEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "en_name") val enName: String,
    @ColumnInfo(name = "alt_names") val altNames: List<String>,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "image") val image: String,
    @ColumnInfo(name = "production") val production: String?,
    @ColumnInfo(name = "release_year") val releaseYear: String?,
    @ColumnInfo(name = "type") val type: ContentType,
    @ColumnInfo(name = "kind") val kind: ContentKind,
    @ColumnInfo(name = "status") val status: ReleaseStatus,
    @ColumnInfo(name = "episodes") val episodes: Int,
    @ColumnInfo(name = "episodes_aired") val episodesAired: Int?,
    @ColumnInfo(name = "episode_duration") val episodeDuration: Int?,
    @ColumnInfo(name = "shirabox_id") val shiraboxId: Int?,
    @ColumnInfo(name = "shikimori_id") val shikimoriID: Int,
    @ColumnInfo(name = "genres") val genres: List<String> = emptyList(),
    @ColumnInfo(name = "favourite") val isFavourite: Boolean,
    @ColumnInfo(name = "episodes_notifications") val episodesNotifications: Boolean,
    @ColumnInfo(name = "last_view") val lastViewTimestamp: Long,
    @ColumnInfo(name = "pinned_teams") val pinnedTeams: List<String>,
    @Embedded val rating: Rating
)
