package live.shirabox.shirabox.ui.screen.explore.notifications

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import live.shirabox.core.entity.NotificationEntity
import live.shirabox.core.entity.relation.NotificationAndContent
import live.shirabox.shirabox.db.AppDatabase

class NotificationsViewModel(context: Context): ViewModel() {
    private val db = AppDatabase.getAppDataBase(context)

    fun allNotificationsFlow(): Flow<List<NotificationEntity>> =
        db?.notificationDao()?.all() ?: emptyFlow()

    fun notificationsWithContentFlow(): Flow<List<NotificationAndContent>> =
        db?.notificationDao()?.allNotificationsWithContent() ?: emptyFlow()

    fun removeNotification(entity: NotificationEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db?.notificationDao()?.deleteNotification(entity)
        }
    }

    fun clearNotifications() {
        viewModelScope.launch(Dispatchers.IO) {
            db?.notificationDao()?.deleteAll()
        }
    }
}