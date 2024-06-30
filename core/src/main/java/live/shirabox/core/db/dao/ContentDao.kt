package live.shirabox.core.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import live.shirabox.core.entity.ContentEntity
import live.shirabox.core.entity.relation.CombinedContent

@Dao
interface ContentDao {
    @Transaction
    @Query("SELECT * FROM content")
    fun allContent(): Flow<List<ContentEntity>>

    @Query("SELECT * FROM content WHERE favourite IS 1")
    fun getFavourites(): Flow<List<ContentEntity>>

    @Transaction
    @Query("SELECT * FROM content WHERE shikimori_id IS :shikimoriId")
    fun getCombinedContent(shikimoriId: Int): CombinedContent

    @Transaction
    @Query("SELECT * FROM content")
    fun getAllCombinedContent(): Flow<List<CombinedContent>>

    @Query("SELECT * FROM content WHERE shikimori_id IS :shikimoriId")
    fun getContent(shikimoriId: Int): ContentEntity?

    @Query("SELECT * FROM content WHERE uid IS :uid")
    fun getContentByUid(uid: Long): ContentEntity

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertContents(vararg contents: ContentEntity): List<Long>

    @Update
    fun updateContents(vararg contents: ContentEntity)

    @Delete
    fun deleteContents(vararg contents: ContentEntity)
}