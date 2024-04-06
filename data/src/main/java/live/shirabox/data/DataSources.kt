package live.shirabox.data

import live.shirabox.data.content.anime.libria.LibriaRepository

object DataSources {
    val contentSources = listOf(
        LibriaRepository()
    )
}