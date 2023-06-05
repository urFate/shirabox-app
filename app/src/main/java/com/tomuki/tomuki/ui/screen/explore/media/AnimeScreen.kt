package com.tomuki.tomuki.ui.screen.explore.media

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tomuki.tomuki.R
import com.tomuki.tomuki.model.ContentType
import com.tomuki.tomuki.ui.screen.explore.ExploreViewModel

@Composable
fun AnimeScreen(viewModel: ExploreViewModel, scrollState: ScrollState) {
    BaseMediaScreen(
        ongoingsTitle = stringResource(id = R.string.anime_actual),
        contentType = ContentType.ANIME,
        viewModel = viewModel,
        scrollState = scrollState
    )
}