package org.shirabox.core.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.shirabox.core.entity.DownloadEntity

@Dao
interface DownloadDao {
    @Query("SELECT * FROM download")
    fun all(): Flow<List<DownloadEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertDownload(vararg downloadEntity: DownloadEntity)

    @Update
    fun updateDownload(vararg downloadEntity: DownloadEntity)

    @Delete
    fun deleteDownload(vararg downloadEntity: DownloadEntity)

    @Query("DELETE FROM download")
    fun deleteAll()
}
