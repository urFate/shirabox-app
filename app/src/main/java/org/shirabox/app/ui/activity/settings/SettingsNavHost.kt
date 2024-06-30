package org.shirabox.app.ui.activity.settings

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import org.shirabox.app.ui.activity.settings.category.AboutSettingsScreen
import org.shirabox.app.ui.activity.settings.category.AppearanceSettingsScreen
import org.shirabox.app.ui.activity.settings.category.GeneralSettingsScreen
import org.shirabox.app.ui.activity.settings.category.SettingsRootScreen
import org.shirabox.app.ui.activity.settings.category.playback.PlaybackSettingsScreen

@Composable
fun SettingsNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "settings",
        enterTransition = {
            fadeIn(
                animationSpec = tween(
                    300, easing = LinearEasing
                )
            ) + slideIntoContainer(
                animationSpec = tween(300, easing = EaseInOut),
                towards = AnimatedContentTransitionScope.SlideDirection.Left
            )
        },
        exitTransition = {
            fadeOut(
                animationSpec = tween(
                    300, easing = LinearEasing
                )
            ) + slideOutOfContainer(
                animationSpec = tween(300, easing = EaseInOut),
                towards = AnimatedContentTransitionScope.SlideDirection.Right
            )
        }

    ){
        navigation(SettingsNavItems.Root.route, "settings") {
            composable(SettingsNavItems.Root.route) { SettingsRootScreen(navController) }

            composable(
                route = SettingsNavItems.General.route
            ) { GeneralSettingsScreen() }

            composable(
                route = SettingsNavItems.Appearance.route
            ) { AppearanceSettingsScreen() }

            composable(
                route = SettingsNavItems.Playback.route
            ) { PlaybackSettingsScreen() }

            composable(
                route = SettingsNavItems.About.route
            ) { AboutSettingsScreen() }
        }
    }
}

