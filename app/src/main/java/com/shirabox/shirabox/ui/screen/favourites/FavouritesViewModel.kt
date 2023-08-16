package com.shirabox.shirabox.ui.screen.favourites

import android.content.Context
import androidx.lifecycle.ViewModel
import com.shirabox.shirabox.db.AppDatabase
import com.shirabox.shirabox.db.entity.ContentEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class FavouritesViewModel(context: Context) : ViewModel() {
    val db = AppDatabase.getAppDataBase(context)

    fun fetchFavouriteContents(): Flow<List<ContentEntity>> =
        db?.contentDao()?.getFavourites() ?: emptyFlow()
}