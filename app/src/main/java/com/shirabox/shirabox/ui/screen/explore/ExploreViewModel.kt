package com.shirabox.shirabox.ui.screen.explore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shirabox.shirabox.model.Content
import com.shirabox.shirabox.model.ContentType
import com.shirabox.shirabox.source.catalog.shikimori.Shikimori
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExploreViewModel : ViewModel() {
    private val animeOngoings = mutableStateListOf<Content>()
    private val animePopulars = mutableStateListOf<Content>()

    private val mangaOngoings = mutableStateListOf<Content>()
    private val mangaPopulars = mutableStateListOf<Content>()

    private val ranobeOngoings = mutableStateListOf<Content>()
    private val ranobePopulars = mutableStateListOf<Content>()

    var currentContentType by mutableStateOf(ContentType.ANIME)

    internal fun currentOngoings() = when(currentContentType) {
        ContentType.ANIME -> animeOngoings
        ContentType.MANGA -> mangaOngoings
        ContentType.RANOBE -> ranobeOngoings
    }

    internal fun currentPopulars() = when(currentContentType) {
        ContentType.ANIME -> animePopulars
        ContentType.MANGA -> mangaPopulars
        ContentType.RANOBE -> ranobePopulars
    }

    fun fetchOngoings(page: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            currentOngoings().addAll(Shikimori.fetchOngoings(page, currentContentType))
        }
    }

    fun fetchPopulars(page: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            currentPopulars().addAll(Shikimori.fetchPopulars(page, currentContentType))
        }
    }
}