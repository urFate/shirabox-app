package org.shirabox.app.ui.component.navigation.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController

@Composable
fun ExploreNavigationView(lazyListState: LazyListState) {
    val navController = rememberNavController()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ExploreNavigationBar(navController)
        ExploreNavHost(navController = navController, lazyListState = lazyListState)
    }
}