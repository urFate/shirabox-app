package live.shirabox.shirabox.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import live.shirabox.core.entity.EpisodeEntity

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM episode")
    fun all(): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episode WHERE content_uid = :contentUid AND episode = :episode LIMIT 1")
    fun getEpisodeByParentAndEpisode(contentUid: Long, episode: Int): EpisodeEntity

    @Query("SELECT * FROM episode WHERE content_uid = :contentUid")
    fun getEpisodesByParent(contentUid: Long): Flow<List<EpisodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEpisodes(vararg episodeEntity: EpisodeEntity)

    @Update
    fun updateEpisodes(vararg episodeEntity: EpisodeEntity)

    @Delete
    fun deleteEpisodes(vararg episodeEntity: EpisodeEntity)

}