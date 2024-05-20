package live.shirabox.shirabox.ui.component.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import live.shirabox.shirabox.ui.screen.explore.ExploreScreen
import live.shirabox.shirabox.ui.screen.explore.notifications.NotificationsScreen
import live.shirabox.shirabox.ui.screen.favourites.FavouritesScreen
import live.shirabox.shirabox.ui.screen.profile.ProfileScreen
import live.shirabox.shirabox.ui.screen.profile.history.History

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiraBoxNavHost(navController: NavHostController) {
    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = "main",
        enterTransition = { fadeIn(animationSpec = tween(400, delayMillis = 150)) },
        exitTransition = { fadeOut(tween(delayMillis = 90)) },
        popEnterTransition = { fadeIn(animationSpec = tween(400, delayMillis = 150)) },
        popExitTransition = { fadeOut(tween(delayMillis = 90)) }
    ) {
        navigation(BottomNavItems.Explore.route, "main") {
            composable(BottomNavItems.Explore.route) { ExploreScreen(navController) }
            composable(BottomNavItems.Favourites.route) { FavouritesScreen(navController) }
            composable(BottomNavItems.Profile.route) { ProfileScreen(navController) }
        }
        composable(NestedNavItems.History.route) { History(navController) }
        composable(NestedNavItems.Notifications.route) { NotificationsScreen(navController) }
    }
}