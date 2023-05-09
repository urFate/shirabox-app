package com.tomuki.tomuki.ui.component.navigation

import com.tomuki.tomuki.R

sealed class ProfileNavItems(
    val name: Int,
    val route: String,
) {
    object History : ProfileNavItems(R.string.history, "history")

}