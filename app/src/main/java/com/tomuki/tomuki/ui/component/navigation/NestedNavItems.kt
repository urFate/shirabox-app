package com.tomuki.tomuki.ui.component.navigation

sealed class NestedNavItems(
    val route: String
) {
    object Notifications : NestedNavItems("notifications")
    object History : NestedNavItems("history")
}