package live.shirabox.shirabox.ui.screen.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import live.shirabox.shirabox.ui.component.top.TopBar


@ExperimentalMaterial3Api
@Composable
fun ExploreScreen(navController: NavController) {

    val lazyListState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { TopBar(navController) }
        item { BaseMediaScreen(lazyListState = lazyListState) }
    }
}

