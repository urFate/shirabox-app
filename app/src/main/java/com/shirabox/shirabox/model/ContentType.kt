package com.shirabox.shirabox.model

enum class ContentType {
    ANIME, MANGA, RANOBE;

    companion object {
        fun fromString(string: String) : ContentType {
            when {
                string.contains("anime", true) -> return ANIME
                string.contains("manga", true) -> return MANGA
                string.contains("ranobe", true) -> return RANOBE
            }
            return ANIME
        }
    }
}