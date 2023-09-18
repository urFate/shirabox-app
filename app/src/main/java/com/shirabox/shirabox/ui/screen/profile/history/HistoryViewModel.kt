package com.shirabox.shirabox.ui.screen.profile.history

import android.content.Context
import androidx.lifecycle.ViewModel
import com.shirabox.shirabox.db.AppDatabase
import com.shirabox.shirabox.db.entity.ContentEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class HistoryViewModel(context: Context) : ViewModel() {
    val db = AppDatabase.getAppDataBase(context)

    fun contentsFlow(): Flow<List<ContentEntity>> =
        db?.contentDao()?.allCollectedContent() ?: emptyFlow()
}