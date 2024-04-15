package live.shirabox.data.content

import live.shirabox.data.content.anime.animelib.AniLibRepository
import live.shirabox.data.content.anime.libria.LibriaRepository

object ContentRepositoryRegistry {
    val REPOSITORIES = listOf(
        LibriaRepository(), AniLibRepository()
    )
}