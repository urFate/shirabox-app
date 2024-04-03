package live.shirabox.core.util

import java.security.MessageDigest

@OptIn(ExperimentalStdlibApi::class)
fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(this.toByteArray())
    return digest.toHexString()
}
fun Double.round(decimals: Int = 2): Double = "%.${decimals}f".format(this).toDouble()