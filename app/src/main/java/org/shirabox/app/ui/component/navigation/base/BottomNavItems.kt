package org.shirabox.app.ui.component.navigation.base

import org.shirabox.app.R

sealed class BottomNavItems(
    val name: Int,
    val route: String,
    val icon: Int,
    val selectedIcon: Int,
    val children: List<String> = emptyList()
) {
    data object Explore : BottomNavItems(R.string.explore, "explore",
        R.drawable.compass, R.drawable.compass_filled,
        listOf(NestedNavItems.Notifications.route)
    )
    data object Favourites : BottomNavItems(R.string.favourites, "favourites",
        R.drawable.star, R.drawable.star_filled)
    data object Profile : BottomNavItems(R.string.profile, "profile",
        R.drawable.user_circle, R.drawable.user_circle_filled,
        listOf(NestedNavItems.History.route)
    )
}

val navItems = listOf(
    BottomNavItems.Explore,
    BottomNavItems.Favourites,
    BottomNavItems.Profile
)

