package live.shirabox.core.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import live.shirabox.core.entity.NotificationEntity
import live.shirabox.core.entity.relation.NotificationAndContent

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notification")
    fun all(): Flow<List<NotificationEntity>>

    @Transaction
    @Query("SELECT * FROM notification")
    fun allNotificationsWithContent(): Flow<List<NotificationAndContent>>

    @Transaction
    @Query("SELECT * FROM notification WHERE content_shikimori_id IS :shikimoriId")
    fun notificationsFromParent(shikimoriId: Int): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertNotification(vararg notificationEntity: NotificationEntity)

    @Update
    fun updateNotification(vararg notificationEntity: NotificationEntity)

    @Delete
    fun deleteNotification(vararg notificationEntity: NotificationEntity)

    @Query("DELETE FROM notification")
    fun deleteAll()
}