package org.shirabox.data.content

import org.shirabox.data.content.anime.animelib.AniLibRepository
import org.shirabox.data.content.anime.libria.LibriaRepository

object ContentRepositoryRegistry {
    val REPOSITORIES = listOf(
        LibriaRepository(), AniLibRepository()
    )
}