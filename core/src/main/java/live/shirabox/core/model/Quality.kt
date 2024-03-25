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
    FHD(1080);

    companion object {
        fun valueOfInt(i: Int): Quality {
            return when(i) {
                480 -> SD
                720 -> HD
                1080 -> FHD
                else -> HD
            }
        }
    }
}