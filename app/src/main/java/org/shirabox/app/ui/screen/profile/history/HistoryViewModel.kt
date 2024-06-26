package org.shirabox.app.ui.screen.profile.history

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.shirabox.core.db.AppDatabase
import org.shirabox.core.entity.ContentEntity
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(@ApplicationContext context: Context) : ViewModel() {
    private val db = AppDatabase.getAppDataBase(context)

    fun contentsFlow(): Flow<List<ContentEntity>> =
        db?.contentDao()?.allContent() ?: emptyFlow()
}