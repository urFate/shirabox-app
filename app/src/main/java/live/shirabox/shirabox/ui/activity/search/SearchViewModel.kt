package live.shirabox.shirabox.ui.activity.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import live.shirabox.core.model.Content
import live.shirabox.core.model.ContentType
import live.shirabox.data.catalog.shikimori.Shikimori

class SearchViewModel : ViewModel() {
    val results = mutableStateListOf<Content>()
    var currentContentType by mutableStateOf(ContentType.ANIME)

    fun search(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            results.apply {
                clear()
                addAll(Shikimori.search(query, currentContentType))
            }
        }
    }
}