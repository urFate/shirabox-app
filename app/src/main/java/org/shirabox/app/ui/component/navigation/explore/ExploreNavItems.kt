package org.shirabox.app.ui.component.navigation.explore

import org.shirabox.app.R

sealed class ExploreNavItems(
    val name: Int,
    val route: String,
) {
    companion object {
        val navItems = listOf(
            PrimaryFeed,
            ScheduleFeed
        )
    }

    data object PrimaryFeed : ExploreNavItems(R.string.primary_feed, "primary_feed")
    data object ScheduleFeed : ExploreNavItems(R.string.schedule_feed, "schedule_feed")
}

