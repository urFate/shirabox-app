package live.shirabox.shirabox.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import live.shirabox.core.entity.EpisodeEntity
import live.shirabox.core.model.ActingTeam

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM episode")
    fun all(): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episode WHERE content_uid = :contentUid AND episode = :episode LIMIT 1")
    fun getEpisodeByParentAndEpisode(contentUid: Long, episode: Int): EpisodeEntity

    @Query("SELECT * FROM episode WHERE content_uid = :contentUid AND acting_team = :actingTeam AND episode = :episode LIMIT 1")
    fun getEpisode(contentUid: Long, episode: Int, actingTeam: ActingTeam): EpisodeEntity

    @Query("SELECT * FROM episode WHERE content_uid = :contentUid AND acting_team = :actingTeam")
    fun getEpisodes(contentUid: Long, actingTeam: ActingTeam): Flow<List<EpisodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEpisodes(vararg episodeEntity: EpisodeEntity)

    @Update
    fun updateEpisodes(vararg episodeEntity: EpisodeEntity)

    @Delete
    fun deleteEpisodes(vararg episodeEntity: EpisodeEntity)

}