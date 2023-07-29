package com.shirabox.shirabox.ui.component.top.navigation

import androidx.compose.ui.graphics.Color
import com.shirabox.shirabox.R
import com.shirabox.shirabox.model.ContentType
import com.shirabox.shirabox.ui.theme.BrandRed
import com.shirabox.shirabox.ui.theme.Purple40
import com.shirabox.shirabox.ui.theme.Tertiary20

sealed class MediaNavItem(
    val name: Int,
    val color: Color,
    val contentType: ContentType
) {

    object Anime : MediaNavItem(R.string.anime, Purple40, ContentType.ANIME)

    object Manga : MediaNavItem(R.string.manga, BrandRed, ContentType.MANGA)

    object Ranobe : MediaNavItem(R.string.ranobe, Tertiary20, ContentType.RANOBE)
}

val mediaNavItems = listOf(
    MediaNavItem.Anime,
    MediaNavItem.Manga,
    MediaNavItem.Ranobe
)