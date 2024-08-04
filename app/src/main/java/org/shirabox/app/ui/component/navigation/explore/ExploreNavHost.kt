package org.shirabox.app.ui.component.navigation.explore

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import org.shirabox.app.ui.screen.explore.feed.primary.PrimaryFeedScreen
import org.shirabox.app.ui.screen.explore.feed.schedule.ScheduleFeedScreen

@Composable
fun ExploreNavHost(
    navController: NavHostController,
    lazyListState: LazyListState
) {
    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = "main_feed",
        enterTransition = { fadeIn(animationSpec = tween(400, delayMillis = 150)) },
        exitTransition = { fadeOut(tween(delayMillis = 90)) },
        popEnterTransition = { fadeIn(animationSpec = tween(400, delayMillis = 150)) },
        popExitTransition = { fadeOut(tween(delayMillis = 90)) }
    ) {
        navigation(ExploreNavItems.PrimaryFeed.route, "main_feed") {
            composable(ExploreNavItems.PrimaryFeed.route) { PrimaryFeedScreen(lazyListState = lazyListState) }
            composable(ExploreNavItems.ScheduleFeed.route) { ScheduleFeedScreen() }
        }
    }
}