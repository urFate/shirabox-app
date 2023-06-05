package com.tomuki.tomuki.ui.component.top.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation

@Composable
fun MediaNavHost(
    navController: NavHostController,
    animeScreen: @Composable (NavBackStackEntry) -> Unit,
    mangaScreen: @Composable (NavBackStackEntry) -> Unit,
    ranobeScreen: @Composable (NavBackStackEntry) -> Unit,
) {
    NavHost(navController = navController, startDestination = "media") {
        navigation(MediaNavItem.Anime.route, "media") {
            composable(route = MediaNavItem.Anime.route, content = animeScreen)
            composable(route = MediaNavItem.Manga.route, content = mangaScreen)
            composable(route = MediaNavItem.Ranobe.route, content = ranobeScreen)
        }
    }
}