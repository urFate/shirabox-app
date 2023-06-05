package com.tomuki.tomuki.ui.screen.explore

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomuki.tomuki.model.Content
import com.tomuki.tomuki.model.ContentType
import com.tomuki.tomuki.source.catalog.shikimori.Shikimori
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExploreViewModel : ViewModel() {
    val animeOngoings = mutableStateOf<List<Content>>(emptyList())
    val animePopulars = mutableStateOf<List<Content>>(emptyList())

    val mangaOngoings = mutableStateOf<List<Content>>(emptyList())
    val mangaPopulars = mutableStateOf<List<Content>>(emptyList())

    val ranobeOngoings = mutableStateOf<List<Content>>(emptyList())
    val ranobePopulars = mutableStateOf<List<Content>>(emptyList())

    fun fetchOngoings(page: Int, contentType: ContentType){
        viewModelScope.launch(Dispatchers.IO) {
            when(contentType){
                ContentType.ANIME -> animeOngoings.value = Shikimori.fetchOngoings(page, contentType)
                ContentType.MANGA -> mangaOngoings.value = Shikimori.fetchOngoings(page, contentType)
                ContentType.RANOBE -> ranobeOngoings.value = Shikimori.fetchOngoings(page, contentType)
            }
        }
    }

    fun fetchPopulars(page: Int, contentType: ContentType){
        viewModelScope.launch(Dispatchers.IO) {
            val contents = Shikimori.fetchPopulars(page, contentType)


            when(contentType){
                ContentType.ANIME -> compareAndCollect(contents, animePopulars)
                ContentType.MANGA -> compareAndCollect(contents, mangaPopulars)
                ContentType.RANOBE -> compareAndCollect(contents, ranobePopulars)
            }

        }
    }

    private fun compareAndCollect(contents: List<Content>, state: MutableState<List<Content>>){
        state.value =
            when {
                contents != state.value -> state.value.plus(contents)
                else -> state.value
            }
    }

    fun isReady(contentType: ContentType) : Boolean {
        return when(contentType){
            ContentType.ANIME -> animeOngoings.value.isNotEmpty() && animePopulars.value.isNotEmpty()
            ContentType.MANGA -> mangaOngoings.value.isNotEmpty() && mangaPopulars.value.isNotEmpty()
            ContentType.RANOBE -> ranobeOngoings.value.isNotEmpty() && ranobePopulars.value.isNotEmpty()
        }
    }
}