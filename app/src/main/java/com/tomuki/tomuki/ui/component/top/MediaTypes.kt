package com.tomuki.tomuki.ui.component.top

import androidx.compose.ui.graphics.Color
import com.tomuki.tomuki.R
import com.tomuki.tomuki.ui.theme.BrandRed
import com.tomuki.tomuki.ui.theme.Purple40
import com.tomuki.tomuki.ui.theme.Tertiary20

sealed class MediaTypes(
    val name: Int,
    val color: Color
) {
    object Anime : MediaTypes(R.string.anime, Purple40)
    object Manga : MediaTypes(R.string.manga, BrandRed)
    object Ranobe : MediaTypes(R.string.ranobe, Tertiary20)
}

val mediaTypesItems = listOf(
    MediaTypes.Anime,
    MediaTypes.Manga,
    MediaTypes.Ranobe
)