package com.shirabox.shirabox.ui.activity.resource

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shirabox.shirabox.model.Content
import com.shirabox.shirabox.model.ContentType
import com.shirabox.shirabox.model.Episode
import com.shirabox.shirabox.model.EpisodesInfo
import com.shirabox.shirabox.source.catalog.shikimori.Shikimori
import com.shirabox.shirabox.source.content.AbstractContentSource
import com.shirabox.shirabox.source.content.anime.libria.AniLibria
import com.shirabox.shirabox.source.content.manga.remanga.Remanga
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ResourceViewModel(val contentType: ContentType) : ViewModel() {
    val content = mutableStateOf<Content?>(null)
    val related = mutableStateListOf<Content>()
    val episodes = mutableStateMapOf<AbstractContentSource, List<Episode>>()
    val episodesInfo = mutableStateMapOf<AbstractContentSource, EpisodesInfo?>()

    val sources = listOf(
        AniLibria, Remanga
    ).filter { it.contentType == contentType }

    fun fetchContent(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            content.value = Shikimori.fetchContent(id, contentType)
        }
    }

    fun fetchRelated(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            Shikimori.fetchRelated(id, contentType).forEach { it?.let(related::add) }
        }
    }

    fun fetchEpisodes(query: String, source: AbstractContentSource){
        viewModelScope.launch(Dispatchers.IO) {
            episodes[source] = source.searchEpisodes(query)
        }
    }

    fun fetchEpisodesInfo(query: String, source: AbstractContentSource) {
        viewModelScope.launch(Dispatchers.IO) {
            episodesInfo[source] = source.searchEpisodesInfo(query)
        }
    }
}