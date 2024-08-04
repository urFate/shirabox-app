package org.shirabox.app.ui.component.navigation.base

sealed class NestedNavItems(
    val route: String
) {
    data object Notifications : NestedNavItems("notifications")
    data object History : NestedNavItems("history")
}