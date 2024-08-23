package org.shirabox.core.model

data class Content(
    val name: String,
    val enName: String,
    val altNames: List<String> = emptyList(),
    val description: String? = null,
    val image: String,
    val production: String? = null,
    val releaseYear: String?,
    val type: ContentType,
    val kind: ContentKind,
    val status: ReleaseStatus,
    val episodes: Int,
    val episodesAired: Int? = null,
    val episodeDuration: Int? = null,
    val rating: Rating,
    val shiraboxId: Int? = null,
    val shikimoriId: Int,
    val genres: List<String> = emptyList()
)