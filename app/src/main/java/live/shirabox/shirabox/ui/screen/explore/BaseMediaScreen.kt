package live.shirabox.shirabox.ui.screen.explore

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material3.fade
import com.google.accompanist.placeholder.material3.placeholder
import live.shirabox.core.model.Content
import live.shirabox.core.util.Util
import live.shirabox.shirabox.R
import live.shirabox.shirabox.ui.activity.resource.ResourceActivity
import live.shirabox.shirabox.ui.component.general.BaseCard
import live.shirabox.shirabox.ui.component.general.ContentCardPlaceholder
import live.shirabox.shirabox.ui.component.general.DespondencyEmoticon
import live.shirabox.shirabox.ui.component.general.ScaredEmoticon
import java.io.IOException

@Composable
fun BaseMediaScreen(
    model: ExploreViewModel = hiltViewModel(),
    lazyListState: LazyListState
) {
    val ongoingsListState = rememberLazyListState()

    val ongoingsStateFlow = model.ongoings.collectAsStateWithLifecycle(initialValue = emptyList())
    val popularsStateFlow = model.populars.collectAsStateWithLifecycle(initialValue = emptyList())
    val trendingFeedStateFlow = model.trendingFeedList.collectAsStateWithLifecycle(initialValue = emptyList())
    val popularsFeedStateFlow = model.popularsFeedList.collectAsStateWithLifecycle(initialValue = emptyList())
    val observationStatus = model.contentObservationStatus

    val isReady = remember(popularsFeedStateFlow.value, trendingFeedStateFlow.value, observationStatus.value) {
        model.isReady()
    }

    LaunchedEffect(null) {
        if(isReady && model.popularsFeedList.value.size > 16) {
            model.popularsFeedList.emit(model.popularsFeedList.value.subList(0, 16))
        }

        model.refresh(true)
    }
    LaunchedEffect(model.popularsPage.intValue) {
        if(isReady) model.fetchPopularsFeed()
    }

    AnimatedVisibility(
        visible = observationStatus.value.status == ExploreViewModel.Status.Failure,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (observationStatus.value.exception) {
                    is IOException -> DespondencyEmoticon(text = stringResource(id = R.string.no_internet_connection_variant))
                    else -> ScaredEmoticon(text = stringResource(id = R.string.no_contents))
                }

                OutlinedButton(onClick = { model.refresh(false) }) {
                    Text(stringResource(id = R.string.refresh))
                }
            }
        }
    }

    if(observationStatus.value.status == ExploreViewModel.Status.Failure) return

    Column(
        modifier = Modifier.animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TrendingFeed(isReady = isReady, contents = trendingFeedStateFlow.value)

            PopularsGrid(contents = popularsStateFlow.value)
        }
        PopularsFeed(isReady = isReady, contents = popularsFeedStateFlow.value)

        LaunchedEffect(lazyListState.canScrollForward) {
            if(!lazyListState.canScrollForward && isReady) model.popularsPage.intValue += 1
        }
    }
}
