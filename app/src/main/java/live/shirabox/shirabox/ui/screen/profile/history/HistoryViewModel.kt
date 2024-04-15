package live.shirabox.shirabox.ui.screen.profile.history

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import live.shirabox.core.entity.ContentEntity
import live.shirabox.shirabox.db.AppDatabase

class HistoryViewModel(context: Context) : ViewModel() {
    private val db = AppDatabase.getAppDataBase(context)

    fun contentsFlow(): Flow<List<ContentEntity>> =
        db?.contentDao()?.allCombinedContent() ?: emptyFlow()
}