package live.shirabox.shirabox.ui.screen.favourites

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import live.shirabox.core.entity.ContentEntity
import live.shirabox.shirabox.db.AppDatabase

class FavouritesViewModel(context: Context) : ViewModel() {
    private val db = AppDatabase.getAppDataBase(context)

    fun fetchFavouriteContents(): Flow<List<ContentEntity>> =
        db?.contentDao()?.getFavourites() ?: emptyFlow()
}