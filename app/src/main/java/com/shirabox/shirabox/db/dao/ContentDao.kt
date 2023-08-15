package com.shirabox.shirabox.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.shirabox.shirabox.db.entity.ContentEntity
import com.shirabox.shirabox.db.relation.PickedContent

@Dao
interface ContentDao {
    @Query("SELECT * FROM content WHERE favourite IS 1")
    fun getFavourites(): List<ContentEntity>

    @Transaction
    @Query("SELECT * FROM content WHERE shikimori_id IS :id")
    fun getPickedContent(id: Int): PickedContent

    @Query("SELECT * FROM content WHERE shikimori_id IS :id")
    fun getContent(id: Int): ContentEntity

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertContents(vararg contents: ContentEntity)

    @Update
    fun updateContents(vararg contents: ContentEntity)

    @Delete
    fun deleteContents(vararg contents: ContentEntity)
}