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
import org.shirabox.core.model.ScheduleEntry
import org.shirabox.data.catalog.shikimori.ShikimoriRepository
import org.shirabox.data.shirabox.ShiraBoxRepository
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(@ApplicationContext context: Context) : ViewModel() {
    private val db = AppDatabase.getAppDataBase(context)!!

    val popularsFeedList = MutableStateFlow(emptyList<Content>())
    val trendingFeedList = MutableStateFlow(emptyList<Content>())
    val scheduleFeedList = MutableStateFlow(emptyList<ScheduleEntry>())
    val historyFeedMap = MutableStateFlow(emptyMap<CombinedContent, EpisodeEntity>())

    val tapeObservationStatus = MutableStateFlow(ObservationStatus(Status.Loading))
    val scheduleObservationStatus = MutableStateFlow(ObservationStatus(Status.Loading))
    val refreshing = MutableStateFlow(false)

    val tapePopularsPage = MutableStateFlow(1)

    private val crashlytics = FirebaseCrashlytics.getInstance()

    private fun fetchTrendingFeed() {
        viewModelScope.launch(Dispatchers.IO) {
            ShikimoriRepository.fetchOngoings(1, ContentType.ANIME)
                .catch {
                    it.printStackTrace()
                    crashlytics.recordException(it)
                    tapeObservationStatus.value = ObservationStatus(Status.Failure, it as Exception)
                    emitAll(emptyFlow())
                }
                .collectLatest {
                    trendingFeedList.emit(it)
                    tapeObservationStatus.value = ObservationStatus(Status.Success)
                }
        }
    }

    fun fetchPopularsFeed() {
        viewModelScope.launch(Dispatchers.IO) {
            ShikimoriRepository.fetchPopulars(1..tapePopularsPage.value, ContentType.ANIME)
                .catch {
                    it.printStackTrace()
                    crashlytics.recordException(it)
                    tapeObservationStatus.value = ObservationStatus(Status.Failure, it as Exception)
                    emitAll(emptyFlow())
                }
                .onCompletion {
                    delay(1000L)
                    refreshing.value = false
                }
                .collectLatest {
                    popularsFeedList.emit(it)
                    tapeObservationStatus.value = ObservationStatus(Status.Success)
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
    
    private fun fetchScheduleFeed() {
        viewModelScope.launch(Dispatchers.IO) { 
            ShiraBoxRepository.fetchSchedule()
                .catch {
                    it.printStackTrace()
                    scheduleObservationStatus.value = ObservationStatus(Status.Failure, it as Exception)
                    emitAll(emptyFlow())
                }
                .collectLatest {
                    scheduleFeedList.emit(it)
                    scheduleObservationStatus.value = ObservationStatus(Status.Success)
                }
        }
    }

    fun cachedContentFlow(id: Int) = db.contentDao().getContentByShiraboxId(id)

    fun refresh(coldStartCheck: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            /**
             * Cold start check is required for first activity start to avoid updating the
             * data every time user returns to the tab
             */

            when(coldStartCheck) {
                true -> {
                    if(tapeObservationStatus.value.status != Status.Success) {
                        fetchTrendingFeed()
                        fetchPopularsFeed()
                    }
                    if(scheduleObservationStatus.value.status != Status.Success) {
                        fetchScheduleFeed()
                    }
                }
                false -> {
                    tapePopularsPage.emit(1)
                    tapeObservationStatus.value = ObservationStatus(Status.Loading)
                    scheduleObservationStatus.value = ObservationStatus(Status.Loading)
                    refreshing.value = true

                    fetchTrendingFeed()
                    fetchPopularsFeed()
                    fetchScheduleFeed()
                }
            }

            fetchHistoryFeed()
        }
    }

    fun isTapeReady(): Boolean =
        popularsFeedList.value.isNotEmpty() && trendingFeedList.value.isNotEmpty()
                && tapeObservationStatus.value.status == Status.Success

    data class ObservationStatus(
        val status: Status,
        val exception: Exception? = null
    )

    enum class Status {
        Loading, Success, Failure
    }
}