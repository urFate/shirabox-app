package com.shirabox.shirabox.ui.screen.explore.media

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material3.fade
import com.google.accompanist.placeholder.material3.placeholder
import com.shirabox.shirabox.R
import com.shirabox.shirabox.model.Content
import com.shirabox.shirabox.model.ContentType
import com.shirabox.shirabox.ui.component.general.CellGrid
import com.shirabox.shirabox.ui.component.general.ContentCard
import com.shirabox.shirabox.ui.screen.explore.ExploreViewModel
import com.shirabox.shirabox.util.Util

@Composable
fun BaseMediaScreen(
    ongoingsTitle: String,
    contentType: ContentType,
    viewModel: ExploreViewModel,
    scrollState: ScrollState
) {
    val isEndReached by remember {
        derivedStateOf {
            scrollState.value == scrollState.maxValue
        }
    }

    var popularsPage by remember {
        mutableStateOf(1)
    }

    LaunchedEffect(popularsPage) {
        viewModel.fetchOngoings(1, contentType)
        viewModel.fetchPopulars(popularsPage, contentType)
    }

    val ongoings = when(contentType) {
        ContentType.ANIME -> viewModel.animeOngoings.value
        ContentType.MANGA -> viewModel.mangaOngoings.value
        ContentType.RANOBE -> viewModel.ranobeOngoings.value
    }
    val populars = when(contentType) {
        ContentType.ANIME -> viewModel.animePopulars.value
        ContentType.MANGA -> viewModel.mangaPopulars.value
        ContentType.RANOBE -> viewModel.ranobePopulars.value
    }

    val isReady = viewModel.isReady(contentType)

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
                        visible = !isReady,
                        highlight = PlaceholderHighlight.fade()
                    ),
                text = ongoingsTitle,
                fontSize = 22.sp,
                fontWeight = FontWeight(500)
            )

            OngoingsRow(isReady = isReady, contents = ongoings)
        }

        Divider(
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

            PopularsGrid(isReady = isReady, contents = populars)
        }

        if(populars.isNotEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            )
        }

        if(isEndReached && isReady){
            LaunchedEffect(true) {
                popularsPage++
            }
        }
    }
}

@Composable
private fun OngoingsRow(isReady: Boolean, contents: List<Content>) {
    val cardWidth = 160
    val dummiesAmount = Util.maxElementsInRow(itemWidth = cardWidth)
    val list = Util.dummyContentsList(isReady, dummiesAmount, contents)

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(list.size) {
            if (it == 0) Spacer(modifier = Modifier.width(16.dp))

            val content = list[it]

            ContentCard(
                modifier = Modifier
                    .size(cardWidth.dp, 220.dp)
                    .placeholder(
                        visible = !isReady,
                        shape = RoundedCornerShape(10),
                        highlight = PlaceholderHighlight.fade()
                    ),
                item = content
            )

            if (it == contents.indices.last) Spacer(modifier = Modifier.width(16.dp))
        }
    }
}

@Composable
private fun PopularsGrid(isReady: Boolean, contents: List<Content>) {
    val cardWidth = 180
    val cardHeight = 240
    val dummiesCount = Util.maxElementsInColumn(itemHeight = cardHeight * 2)
    val list = Util.dummyContentsList(isReady, dummiesCount, contents)

    CellGrid(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        cellWidth = cardWidth.dp,
        contents = list.map {
            {
                ContentCard(
                    modifier = Modifier
                        .placeholder(
                            visible = !isReady,
                            shape = RoundedCornerShape(10),
                            highlight = PlaceholderHighlight.fade()
                        )
                        .size(cardWidth.dp, cardHeight.dp),
                    item = it
                )
            }
        }
    )
}