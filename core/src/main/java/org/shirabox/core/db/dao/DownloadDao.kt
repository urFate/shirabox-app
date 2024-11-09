package org.shirabox.core.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.shirabox.core.entity.DownloadEntity
import org.shirabox.core.entity.relation.DownloadAndContent

@Dao
interface DownloadDao {
    @Transaction
    @Query("SELECT * FROM download")
    fun allWithContent(): Flow<List<DownloadAndContent>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertDownload(vararg downloadEntity: DownloadEntity)

    @Update
    fun updateDownload(vararg downloadEntity: DownloadEntity)

    @Delete
    fun deleteDownload(vararg downloadEntity: DownloadEntity)

    @Query("DELETE FROM download")
    fun deleteAll()
}
