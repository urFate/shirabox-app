package com.shirabox.shirabox.ui.component.top.navigation

import androidx.compose.ui.graphics.Color
import com.shirabox.shirabox.R
import com.shirabox.shirabox.model.ContentType
import com.shirabox.shirabox.ui.theme.light_primary
import com.shirabox.shirabox.ui.theme.mangaPrimary
import com.shirabox.shirabox.ui.theme.ranobePrimary

sealed class MediaNavItem(
    val name: Int,
    val color: Color,
    val contentType: ContentType
) {

    object Anime : MediaNavItem(R.string.anime, light_primary, ContentType.ANIME)

    object Manga : MediaNavItem(R.string.manga, mangaPrimary, ContentType.MANGA)

    object Ranobe : MediaNavItem(R.string.ranobe, ranobePrimary, ContentType.RANOBE)
}

val mediaNavItems = listOf(
    MediaNavItem.Anime,
    MediaNavItem.Manga,
    MediaNavItem.Ranobe
)