package com.shirabox.shirabox.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shirabox.shirabox.db.entity.RelatedContentEntity

@Dao
interface RelatedDao {
    @Query("SELECT * FROM related")
    fun all(): List<RelatedContentEntity>

    @Query("SELECT * FROM related WHERE content_uid IS :parentId")
    fun getRelatedByParent(parentId: Int): List<RelatedContentEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertRelated(vararg relatedContentEntity: RelatedContentEntity)

    @Update
    fun updateRelated(vararg relatedContentEntity: RelatedContentEntity)

    @Delete
    fun deleteRelated(vararg relatedContentEntity: RelatedContentEntity)
}