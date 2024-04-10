package live.shirabox.shirabox.ui.component.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
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
fun ShiraBoxNavHost(navController: NavHostController){
    NavHost(navController = navController, startDestination = "main"){
        navigation(BottomNavItems.Explore.route, "main") {
            composable(BottomNavItems.Explore.route) { ExploreScreen(navController) }
            composable(BottomNavItems.Favourites.route) { FavouritesScreen(navController) }
            composable(BottomNavItems.Profile.route) { ProfileScreen(navController) }
        }
        composable(NestedNavItems.History.route) { History(navController) }
        composable(NestedNavItems.Notifications.route) { NotificationsScreen(navController) }
    }
}