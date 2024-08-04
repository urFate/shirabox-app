package org.shirabox.app.ui.screen.explore.feed.primary

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.shirabox.app.ui.screen.explore.ExploreViewModel
import org.shirabox.app.ui.screen.explore.TroubleMessage

@Composable
fun PrimaryFeedScreen(
    model: ExploreViewModel = hiltViewModel(),
    lazyListState: LazyListState
) {
    val trendingFeedList by model.trendingFeedList.collectAsStateWithLifecycle(initialValue = emptyList())
    val popularsFeedList by model.popularsFeedList.collectAsStateWithLifecycle(initialValue = emptyList())
    val historyFeedList by model.historyFeedMap.collectAsStateWithLifecycle(initialValue = emptyMap())
    val observationStatus by model.tapeObservationStatus.collectAsStateWithLifecycle()
    val popularsPage by model.tapePopularsPage.collectAsStateWithLifecycle()

    val isReady = remember(popularsFeedList, trendingFeedList, observationStatus, model::isTapeReady)

    LaunchedEffect(null) {
        model.refresh(true)
    }
    LaunchedEffect(null) {
        if(isReady && model.popularsFeedList.value.size > 16) {
            model.popularsFeedList.emit(model.popularsFeedList.value.subList(0, 16))
            model.tapePopularsPage.emit(1)
        }
    }
    LaunchedEffect(popularsPage) {
        if(isReady) model.fetchPopularsFeed()
    }

    TroubleMessage(observationStatus = observationStatus)

    if(observationStatus.status == ExploreViewModel.Status.Failure) return

    Column(
        modifier = Modifier.animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PrimaryTrendingFeed(isReady = isReady, contents = trendingFeedList)

        PrimaryHistoryFeed(isReady = isReady, contents = historyFeedList)

        PrimaryPopularsFeed(isReady = isReady, contents = popularsFeedList)

        LaunchedEffect(lazyListState.canScrollForward) {
            if(!lazyListState.canScrollForward && isReady) model.tapePopularsPage.emit(popularsPage.inc())
        }
    }
}
