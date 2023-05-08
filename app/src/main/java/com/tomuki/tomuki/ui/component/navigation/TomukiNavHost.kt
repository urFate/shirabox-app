package com.tomuki.tomuki.ui.component.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tomuki.tomuki.ui.screen.explore.ExploreScreen
import com.tomuki.tomuki.ui.screen.favourites.FavouritesScreen
import com.tomuki.tomuki.ui.screen.profile.ProfileScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TomukiNavHost(navController: NavHostController){
    NavHost(navController = navController, startDestination = BottomNavItems.Explore.route){
        composable(BottomNavItems.Explore.route) { ExploreScreen() }
        composable(BottomNavItems.Favourites.route) { FavouritesScreen() }
        composable(BottomNavItems.Profile.route) { ProfileScreen() }
    }
}