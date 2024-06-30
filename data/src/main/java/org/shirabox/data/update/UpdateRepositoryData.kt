package org.shirabox.data.update

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppUpdateState(
    @SerialName("release")
    val release: Release,
    @SerialName("updateAvailable")
    val updateAvailable: Boolean
)

@Serializable
data class Release(
    @SerialName("createdAt")
    val createdAt: Long,
    @SerialName("downloadUrl")
    val downloadUrl: String,
    @SerialName("notes")
    val notes: String,
    @SerialName("prerelease")
    val prerelease: Boolean,
    @SerialName("tag")
    val tag: String,
    @SerialName("title")
    val title: String
)