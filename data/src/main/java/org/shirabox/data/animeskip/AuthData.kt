package org.shirabox.data.animeskip

data class AuthData(
    val authToken: String,
    val refreshToken: String,
    val account: AnimeSkipAccount
)
