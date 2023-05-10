package com.tomuki.tomuki.ui.component.navigation

sealed class NestedNavItems(
    val route: String
) {
    object History : NestedNavItems("history")
}