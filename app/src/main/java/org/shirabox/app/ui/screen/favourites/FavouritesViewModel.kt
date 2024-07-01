package org.shirabox.app.ui.screen.favourites

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.shirabox.core.db.AppDatabase
import org.shirabox.core.model.Content
import org.shirabox.core.model.ContentKind
import org.shirabox.core.util.Util
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor(@ApplicationContext context: Context) : ViewModel() {
    private val db = AppDatabase.getAppDataBase(context)!!
    val selectedSortType = mutableStateOf(SortType.DEFAULT)
    val selectedKind = mutableStateOf<ContentKind?>(null)

    fun fetchFavouriteContents(): Flow<List<Content>> =
        db.contentDao().getFavourites()
            .map { entityList ->
                when (selectedSortType.value) {
                    SortType.ALPHABETICAL -> entityList.sortedByDescending { it.name }
                    SortType.RATING -> entityList.sortedByDescending { it.rating.average }
                    SortType.RECENT -> entityList.sortedByDescending { it.lastViewTimestamp }
                    SortType.STATUS -> entityList.sortedByDescending { it.status.ordinal }
                    SortType.DEFAULT -> entityList.reversed()
                }
            }
            .map { entityList ->
                entityList
                    .takeIf { selectedKind.value != null }
                    ?.filter { it.kind == selectedKind.value } ?: entityList
            }
            .map { entityList -> entityList.map { Util.mapEntityToContent(it) } }
}

enum class SortType {
    DEFAULT, ALPHABETICAL, RATING, RECENT, STATUS
}