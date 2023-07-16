package com.shirabox.shirabox.ui.screen.explore.media

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.shirabox.shirabox.R
import com.shirabox.shirabox.model.ContentType
import com.shirabox.shirabox.ui.screen.explore.ExploreViewModel

@Composable
fun AnimeScreen(viewModel: ExploreViewModel, scrollState: ScrollState) {
    BaseMediaScreen(
        ongoingsTitle = stringResource(id = R.string.anime_actual),
        contentType = ContentType.ANIME,
        viewModel = viewModel,
        scrollState = scrollState
    )
}