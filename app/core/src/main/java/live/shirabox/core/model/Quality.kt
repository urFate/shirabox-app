package live.shirabox.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Quality(val quality: Int) {
    @SerialName("SD")
    SD(480),

    @SerialName("HD")
    HD(720),

    @SerialName("FHD")
    FHD(1080)
}