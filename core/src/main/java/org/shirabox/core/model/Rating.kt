package org.shirabox.core.model

data class Rating(
    val average: Double,
    val scores: Map<Int, Int> = emptyMap()
)