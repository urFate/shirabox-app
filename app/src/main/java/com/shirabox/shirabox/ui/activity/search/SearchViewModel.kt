package com.shirabox.shirabox.ui.activity.search

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