package org.shirabox.app.ui.component.navigation.base

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.ui.graphics.vector.ImageVector
import org.shirabox.app.R

sealed class BottomNavItems(
    val name: Int,
    val route: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val children: List<String> = emptyList()
) {
    data object Explore : BottomNavItems(R.string.explore, "explore",
        Icons.Outlined.Explore, Icons.Filled.Explore,
        listOf(NestedNavItems.Notifications.route)
    )
    data object Favourites : BottomNavItems(R.string.favourites, "favourites",
        Icons.Outlined.BookmarkBorder, Icons.Filled.Bookmark)
    data object Profile : BottomNavItems(R.string.profile, "profile",
        Icons.Outlined.AccountCircle, Icons.Filled.AccountCircle,
        listOf(NestedNavItems.History.route)
    )
}

val navItems = listOf(
    BottomNavItems.Explore,
    BottomNavItems.Favourites,
    BottomNavItems.Profile
)

