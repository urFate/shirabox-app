package live.shirabox.core.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import live.shirabox.core.model.ContentType
import live.shirabox.core.model.Rating

@Entity(tableName = "content")
data class ContentEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "en_name") val enName: String,
    @ColumnInfo(name = "alt_names") val altNames: List<String>,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "image") val image: String,
    @ColumnInfo(name = "production") val production: String?,
    @ColumnInfo(name = "release_year") val releaseYear: String?,
    @ColumnInfo(name = "type") val type: ContentType,
    @ColumnInfo(name = "kind") val kind: String,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "episodes") val episodes: Int,
    @ColumnInfo(name = "episodes_aired") val episodesAired: Int?,
    @ColumnInfo(name = "episode_duration") val episodeDuration: Int?,
    @Embedded val rating: Rating,
    @ColumnInfo(name = "shikimori_id") val shikimoriID: Int,
    @ColumnInfo(name = "genres") val genres: List<String> = emptyList(),
    @ColumnInfo(name = "favourite") val isFavourite: Boolean,
    @ColumnInfo(name = "last_view") val lastViewTimestamp: Long,
    @ColumnInfo(name = "pinned_sources") val pinnedSources: List<String>
)
