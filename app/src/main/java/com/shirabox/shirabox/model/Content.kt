package com.shirabox.shirabox.model

data class Content(
    val name: String,
    val altName: String,
    val description: String? = null,
    val image: String,
    val production: String? = null,
    val releaseYear: String?,
    val type: ContentType,
    val kind: String,
    val status: String,
    val episodes: Int,
    val episodesAired: Int? = null,
    val episodeDuration: Int? = null,
    val rating: Rating,
    val shikimoriID: Int,
    val genres: List<String> = emptyList()
)