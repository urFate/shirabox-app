package org.shirabox.app.ui.screen.explore.notifications

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.shirabox.core.db.AppDatabase
import org.shirabox.core.entity.NotificationEntity
import org.shirabox.core.entity.relation.NotificationAndContent
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(@ApplicationContext context: Context) : ViewModel() {
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