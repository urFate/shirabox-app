package live.shirabox.shirabox.ui.screen.explore.notifications

import android.content.Context
import android.util.Log
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
    private val appDatabase = AppDatabase.getAppDataBase(context)
    val notificationsWithContent = mutableListOf<NotificationAndContent>()

    fun fetchNotifications(): Flow<List<NotificationEntity>> =
        appDatabase?.notificationDao()?.all() ?: emptyFlow()

    fun fetchNotificationsWithContent(codes: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            appDatabase?.let { db ->
                notificationsWithContent.clear()

                codes.forEach {
                    Log.d("NotificationsViewModel", "Code: $it")
                    notificationsWithContent.add(db.notificationDao().notificationWithContent(it))
                }
            }
            Log.d("NotificationsViewModel", "NC Size: ${notificationsWithContent.size}")
        }
    }

    fun clearNotifications() {
        viewModelScope.launch(Dispatchers.IO) {
            appDatabase?.notificationDao()?.deleteAll()
        }
    }
}