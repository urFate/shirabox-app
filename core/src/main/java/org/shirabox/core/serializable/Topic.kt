package org.shirabox.core.serializable

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Topic(
    val repository: String,
    @SerialName("acting_team") val actingTeam: String,
    val md5: String
)