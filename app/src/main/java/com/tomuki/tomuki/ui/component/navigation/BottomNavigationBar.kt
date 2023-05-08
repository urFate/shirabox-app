package com.tomuki.tomuki.ui.component.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(navController: NavController, items: List<BottomNavItems> = navItems) {
    NavigationBar {
        val navBackStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry.value?.destination

        items.forEach { item ->
            val selected = currentRoute?.hierarchy?.any {
                item.route == it.route
            } == true

            val icon = if(selected) item.selectedIcon else item.icon

            NavigationBarItem(
                label = { Text(stringResource(item.name)) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = "${item.name} Icon",
                    )
                },
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                    }
                }
            )
        }
    }
}