package org.shirabox.app.ui.screen.explore

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import org.shirabox.core.db.AppDatabase
import org.shirabox.core.entity.EpisodeEntity
import org.shirabox.core.entity.relation.CombinedContent
import org.shirabox.core.model.Content
import org.shirabox.core.model.ContentType
import org.shirabox.data.catalog.shikimori.ShikimoriRepository
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(@ApplicationContext context: Context) : ViewModel() {
    private val db = AppDatabase.getAppDataBase(context)!!

    val popularsFeedList = MutableStateFlow(emptyList<Content>())
    val trendingFeedList = MutableStateFlow(emptyList<Content>())
    val historyFeedMap = MutableStateFlow(emptyMap<CombinedContent, EpisodeEntity>())

    val contentObservationStatus = MutableStateFlow(ObservationStatus(Status.Loading))
    val popularsPage = MutableStateFlow(1)
    val refreshing = MutableStateFlow(false)

    private val crashlytics = FirebaseCrashlytics.getInstance()

    private fun fetchTrendingFeed() {
        viewModelScope.launch(Dispatchers.IO) {
            ShikimoriRepository.fetchOngoings(1, ContentType.ANIME)
                .catch {
                    it.printStackTrace()
                    crashlytics.recordException(it)
                    contentObservationStatus.value = ObservationStatus(Status.Failure, it as Exception)
                    emitAll(emptyFlow())
                }
                .collectLatest {
                    trendingFeedList.emit(it)
                    contentObservationStatus.value = ObservationStatus(Status.Success)
                }
        }
    }

    fun fetchPopularsFeed() {
        viewModelScope.launch(Dispatchers.IO) {
            ShikimoriRepository.fetchPopulars(1..popularsPage.value, ContentType.ANIME)
                .catch {
                    it.printStackTrace()
                    crashlytics.recordException(it)
                    contentObservationStatus.value = ObservationStatus(Status.Failure, it as Exception)
                    emitAll(emptyFlow())
                }
                .onCompletion {
                    delay(1000L)
                    refreshing.value = false
                }
                .collectLatest {
                    popularsFeedList.emit(it)
                    contentObservationStatus.value = ObservationStatus(Status.Success)
                }
        }
    }

    private fun fetchHistoryFeed() {
        viewModelScope.launch(Dispatchers.IO) {
            db.contentDao().getAllCombinedContent()
                .map { list ->
                    list.sortedByDescending { it.content.lastViewTimestamp }
                }
                .collectLatest { contents ->
                    val episodesMap = mutableMapOf<CombinedContent, EpisodeEntity>()

                    contents.forEach { combinedContent ->
                        val candidate = combinedContent.episodes
                            .filter { (it.videoLength != null && it.viewTimestamp != null) }
                            .maxByOrNull { it.viewTimestamp ?: 0L }

                        // Put episode if it's aren't finished
                        candidate?.let { entity ->
                            if (entity.watchingTime < entity.videoLength!!) episodesMap[combinedContent] =
                                entity
                        }
                    }

                    historyFeedMap.emit(
                        episodesMap.toSortedMap(compareByDescending { it.content.lastViewTimestamp })
                    )
                }
        }
    }

    fun refresh(coldStartCheck: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            /**
             * Cold start check is required for first activity start to avoid updating the
             * data every time user returns to the tab
             */

            when(coldStartCheck) {
                true -> {
                    if(contentObservationStatus.value.status != Status.Success) {
                        fetchTrendingFeed()
                        fetchPopularsFeed()
                    }
                }
                false -> {
                    popularsPage.emit(1)
                    contentObservationStatus.value = ObservationStatus(Status.Loading)
                    refreshing.value = true

                    fetchTrendingFeed()
                    fetchPopularsFeed()
                }
            }

            fetchHistoryFeed()
        }
    }

    fun isReady() : Boolean {
        return popularsFeedList.value.isNotEmpty() && trendingFeedList.value.isNotEmpty()
                && contentObservationStatus.value.status == Status.Success
    }

    data class ObservationStatus(
        val status: Status,
        val exception: Exception? = null
    )

    enum class Status {
        Loading, Success, Failure
    }
}