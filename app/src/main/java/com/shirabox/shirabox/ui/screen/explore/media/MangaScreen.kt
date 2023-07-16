package com.shirabox.shirabox.ui.screen.explore.media

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.shirabox.shirabox.R
import com.shirabox.shirabox.model.ContentType
import com.shirabox.shirabox.ui.screen.explore.ExploreViewModel

@Composable
fun MangaScreen(viewModel: ExploreViewModel, scrollState: ScrollState) {
    BaseMediaScreen(
        ongoingsTitle = stringResource(id = R.string.books_actual),
        contentType = ContentType.MANGA,
        viewModel = viewModel,
        scrollState = scrollState
    )
}
