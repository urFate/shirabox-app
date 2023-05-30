package com.tomuki.tomuki.model

data class Rating(
    val average: Double,
    val scores: Map<Int, Int> = emptyMap()
)