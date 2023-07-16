package com.shirabox.shirabox.model

data class Content(
    val name: String,
    val altName: String,
    val description: String = "",
    val coverUri: String,
    val production: String = "",
    val releaseYear: String,
    val type: ContentType,
    val kind: String,
    val episodesCount: Int,
    val rating: Rating,
    val shikimoriID: Int,
    val genres: List<String> = emptyList()
)