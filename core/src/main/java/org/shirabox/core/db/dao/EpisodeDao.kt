package org.shirabox.core.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.shirabox.core.entity.EpisodeEntity

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM episode")
    fun all(): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episode WHERE content_uid = :contentUid AND episode = :episode LIMIT 1")
    fun getEpisodeByParentAndEpisode(contentUid: Long, episode: Int): EpisodeEntity

    @Query("SELECT * FROM episode WHERE content_uid = :contentUid AND acting_team_name = :team AND source = :repository AND episode = :episode LIMIT 1")
    fun getEpisode(contentUid: Long, episode: Int, team: String, repository: String): EpisodeEntity

    @Query("SELECT * FROM episode WHERE content_uid = :contentUid AND acting_team_name = :team AND source = :repository")
    fun getEpisodes(contentUid: Long, team: String, repository: String): Flow<List<EpisodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEpisodes(vararg episodeEntity: EpisodeEntity)

    @Update
    fun updateEpisodes(vararg episodeEntity: EpisodeEntity)

    @Delete
    fun deleteEpisodes(vararg episodeEntity: EpisodeEntity)

}