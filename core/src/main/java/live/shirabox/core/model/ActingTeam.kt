package live.shirabox.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActingTeam(
    val name: String,
    @SerialName("logo_url")
    val logoUrl: String
)
