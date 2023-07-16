package com.shirabox.shirabox.ui.screen.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.shirabox.shirabox.R
import com.shirabox.shirabox.ui.component.top.TopBar
import com.shirabox.shirabox.ui.component.top.navigation.MediaNavBar
import com.shirabox.shirabox.ui.component.top.navigation.MediaNavHost
import com.shirabox.shirabox.ui.screen.explore.media.AnimeScreen
import com.shirabox.shirabox.ui.screen.explore.media.MangaScreen
import com.shirabox.shirabox.ui.screen.explore.media.RanobeScreen

@ExperimentalMaterial3Api
@Composable
fun ExploreScreen(navController: NavController, model: ExploreViewModel = viewModel()) {

    val scrollState = rememberScrollState()
    val mediaNavController = rememberNavController()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopBar(stringResource(R.string.search_by_name), navController)
        
        MediaNavBar(navController = mediaNavController)

        MediaNavHost(
            navController = mediaNavController,
            animeScreen = {
                AnimeScreen(
                    viewModel = model,
                    scrollState = scrollState
                )
            },
            mangaScreen = {
                MangaScreen(
                    viewModel = model,
                    scrollState = scrollState
                )
            },
            ranobeScreen = {
                RanobeScreen(
                    viewModel = model,
                    scrollState = scrollState
                )
            }
        )
    }
}

