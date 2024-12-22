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
    @SerialName("uploads")
    val uploads: Uploads,
    @SerialName("notes")
    val notes: String,
    @SerialName("tag")
    val tag: String,
    @SerialName("title")
    val title: String
)

@Serializable
data class Uploads(
    @SerialName("universal")
    val universal: String,
    @SerialName("x86_64")
    val amd64: String? = null,
    @SerialName("arm64-v8a")
    val armV8: String? = null,
    @SerialName("armeabi-v7a")
    val armV7: String? = null
)