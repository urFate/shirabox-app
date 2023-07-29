package com.shirabox.shirabox.ui.screen.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.shirabox.shirabox.R
import com.shirabox.shirabox.ui.component.top.TopBar
import com.shirabox.shirabox.ui.component.top.navigation.MediaNavBar


@ExperimentalMaterial3Api
@Composable
fun ExploreScreen(navController: NavController, model: ExploreViewModel = viewModel()) {

    val lazyListState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { TopBar(stringResource(R.string.search_by_name), navController) }
        
        item {
            MediaNavBar(activeType = model.currentContentType) {
                model.currentContentType = it.contentType
            }
        }

        item {
            BaseMediaScreen(viewModel = model, lazyListState = lazyListState)
        }
    }
}

