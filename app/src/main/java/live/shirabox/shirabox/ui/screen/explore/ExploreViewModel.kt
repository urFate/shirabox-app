package live.shirabox.shirabox.ui.screen.explore

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import live.shirabox.core.model.Content
import live.shirabox.core.model.ContentType
import live.shirabox.data.catalog.shikimori.ShikimoriRepository

class ExploreViewModel : ViewModel() {
    val populars = MutableStateFlow(emptyList<Content>())
    val ongoings = MutableStateFlow(emptyList<Content>())

    val contentObservationStatus = mutableStateOf(ObservationStatus(Status.Loading))
    val popularsPage = mutableIntStateOf(1)
    val refreshing = mutableStateOf(false)

    fun fetchOngoings() {
        viewModelScope.launch(Dispatchers.IO) {
            ShikimoriRepository.fetchOngoings(1, ContentType.ANIME)
                .catch {
                    it.printStackTrace()
                    contentObservationStatus.value = ObservationStatus(Status.Failure, it as Exception)
                    emitAll(emptyFlow())
                }.collectLatest {
                    ongoings.emit(it)
                    contentObservationStatus.value = ObservationStatus(Status.Success)
                }
        }
    }

    fun fetchPopulars() {
        viewModelScope.launch(Dispatchers.IO) {
            ShikimoriRepository.fetchPopulars(1..popularsPage.intValue, ContentType.ANIME)
                .catch {
                    it.printStackTrace()
                    contentObservationStatus.value = ObservationStatus(Status.Failure, it as Exception)
                    emitAll(emptyFlow())
                }.onCompletion {
                    delay(1000L)
                    refreshing.value = false
                }
                .collectLatest {
                    populars.emit(it)
                    contentObservationStatus.value = ObservationStatus(Status.Success)
                }
        }
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            popularsPage.intValue = 1
            contentObservationStatus.value = ObservationStatus(Status.Loading)
            refreshing.value = true

            fetchOngoings()
            fetchPopulars()
        }
    }

    data class ObservationStatus(
        val status: Status,
        val exception: Exception? = null
    )

    enum class Status {
        Loading, Success, Failure
    }
}