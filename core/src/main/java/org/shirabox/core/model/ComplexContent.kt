package org.shirabox.core.model

import org.shirabox.core.entity.ContentEntity

data class ComplexContent(
    val content: ContentEntity,
    val shiraBoxAnime: ShiraBoxAnime?
)
