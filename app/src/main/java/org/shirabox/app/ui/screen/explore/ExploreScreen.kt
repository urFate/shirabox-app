package org.shirabox.app.ui.screen.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.shirabox.app.ui.component.top.TopBar


@OptIn(ExperimentalMaterialApi::class)
@ExperimentalMaterial3Api
@Composable
fun ExploreScreen(
    navController: NavController,
    model: ExploreViewModel = hiltViewModel()
) {
    val isRefreshing by model.refreshing.collectAsStateWithLifecycle()

    val lazyListState = rememberLazyListState()
    val pullRefreshState = rememberPullRefreshState(isRefreshing, { model.refresh(false) })

    Scaffold(
        modifier = Modifier.pullRefresh(pullRefreshState),
        snackbarHost = { AppUpdateSnackbarHost() },
        contentWindowInsets = WindowInsets(0.dp)
        ) {
        Box (
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            contentAlignment = Alignment.TopCenter
        ){
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f),
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { TopBar(navController) }
                item { BaseMediaScreen(lazyListState = lazyListState) }
            }

            PullRefreshIndicator(
                modifier = Modifier.zIndex(2f),
                refreshing = model.refreshing.value,
                state = pullRefreshState,
                scale = true,
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

