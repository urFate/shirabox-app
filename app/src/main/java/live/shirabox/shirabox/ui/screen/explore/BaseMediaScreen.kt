package live.shirabox.shirabox.ui.screen.explore

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material3.fade
import com.google.accompanist.placeholder.material3.placeholder
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import live.shirabox.core.model.Content
import live.shirabox.core.model.ContentType
import live.shirabox.core.util.Util
import live.shirabox.data.catalog.shikimori.ShikimoriRepository
import live.shirabox.shirabox.R
import live.shirabox.shirabox.ui.activity.resource.ResourceActivity
import live.shirabox.shirabox.ui.component.general.BaseCard
import live.shirabox.shirabox.ui.component.general.ContentCardPlaceholder
import live.shirabox.shirabox.ui.component.general.DespondencyEmoticon
import live.shirabox.shirabox.ui.component.general.ScaredEmoticon
import java.io.IOException

@Composable
fun BaseMediaScreen(
    lazyListState: LazyListState
) {
    val popularsPage = remember { mutableIntStateOf(1) }
    val ongoingsListState = rememberLazyListState()

    val contentObservationException = remember<MutableState<Exception?>> {
        mutableStateOf(null)
    }

    val popularsStateFlow =
        ShikimoriRepository.fetchPopulars(1..popularsPage.intValue, ContentType.ANIME)
            .catch {
                it.printStackTrace()
                contentObservationException.value = it as Exception
                emitAll(emptyFlow())
            }
            .collectAsStateWithLifecycle(initialValue = null)
    val ongoingsStateFlow =
        ShikimoriRepository.fetchOngoings(1, ContentType.ANIME)
            .catch {
                it.printStackTrace()
                contentObservationException.value = it as Exception
                emitAll(emptyFlow())
            }
            .collectAsStateWithLifecycle(initialValue = null)

    val isReady = remember(popularsStateFlow.value, ongoingsStateFlow.value) {
        (popularsStateFlow.value != null && ongoingsStateFlow.value != null)
    }

    AnimatedVisibility(visible = contentObservationException.value != null, enter = fadeIn()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (contentObservationException.value) {
                is IOException -> DespondencyEmoticon(text = stringResource(id = R.string.no_internet_connection_variant))
                else -> ScaredEmoticon(text = stringResource(id = R.string.no_contents))
            }
        }
    }

    if(contentObservationException.value != null) return

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                modifier = Modifier
                    .padding(16.dp, 0.dp)
                    .placeholder(
                        visible = !isReady, highlight = PlaceholderHighlight.fade()
                    ),
                text = stringResource(id = R.string.actual),
                fontSize = 22.sp,
                fontWeight = FontWeight(500)
            )

            OngoingsRow(
                isReady = isReady,
                contents = ongoingsStateFlow.value ?: emptyList(),
                ongoingsListState = ongoingsListState
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(32.dp, 12.dp)
        )

        Column(
            modifier = Modifier.padding(16.dp, 0.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                modifier = Modifier
                    .placeholder(
                        visible = !isReady,
                        highlight = PlaceholderHighlight.fade()
                    ),
                text = stringResource(R.string.popular),
                fontSize = 22.sp,
                fontWeight = FontWeight(500)
            )

            PopularsGrid(
                isReady = isReady,
                contents = popularsStateFlow.value ?: emptyList()
            )
        }

        AnimatedVisibility(visible = isReady) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(16.dp)
                )
            }
        }

        LaunchedEffect(lazyListState.canScrollForward) {
            if(!lazyListState.canScrollForward && isReady) popularsPage.intValue += 1
        }
    }
}

@Composable
private fun OngoingsRow(
    isReady: Boolean,
    contents: List<Content>,
    ongoingsListState: LazyListState
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val cardWidth = 160
    val placeholdersAmount = remember {
        Util.maxElementsInRow(itemWidth = cardWidth, configuration = configuration)
    }

    LazyRow(
        state = ongoingsListState,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        if(!isReady) items(placeholdersAmount) {
            ContentCardPlaceholder(
                modifier = Modifier
                    .size(cardWidth.dp, 220.dp)
            )
        }

        items(contents) {
            BaseCard(
                modifier = Modifier.size(cardWidth.dp, 220.dp),
                title = it.name, image = it.image, type = it.type
            ) {
                context.startActivity(
                    Intent(
                        context,
                        ResourceActivity::class.java
                    ).apply {
                        putExtra("id", it.shikimoriID)
                        putExtra("type", it.type.toString())
                    }
                )
            }
        }
    }
}

@Composable
private fun PopularsGrid(isReady: Boolean, contents: List<Content>) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val cardWidth = 180
    val cardHeight = 240
    val columns = remember { configuration.screenWidthDp.floorDiv(cardWidth) }

    val gridHeight by remember(contents) {
        derivedStateOf {
            Util.calcGridHeight(
                itemsCount = if (contents.isEmpty()) 6 else contents.size,
                itemHeight = cardHeight,
                columns = columns
            ).dp
        }
    }

    LazyVerticalGrid(
        modifier = Modifier.height(gridHeight),
        columns = GridCells.Adaptive(cardWidth.minus(32).dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = false
    ) {
        if (!isReady) items(6) {
            ContentCardPlaceholder(modifier = Modifier.size(cardWidth.dp, cardHeight.dp))
        }

        items(contents) {
            BaseCard(
                modifier = Modifier
                    .size(cardWidth.dp, cardHeight.dp),
                title = it.name, image = it.image, type = it.type
            ) {
                context.startActivity(
                    Intent(
                        context,
                        ResourceActivity::class.java
                    ).apply {
                        putExtra("id", it.shikimoriID)
                        putExtra("type", it.type.toString())
                    }
                )
            }
        }
    }
}