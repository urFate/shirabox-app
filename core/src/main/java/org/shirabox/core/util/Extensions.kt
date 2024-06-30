package org.shirabox.core.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.security.MessageDigest

@OptIn(ExperimentalStdlibApi::class)
fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(this.toByteArray())
    return digest.toHexString()
}
fun Double.round(decimals: Int = 2): Double {
    return BigDecimal(this).setScale(decimals, RoundingMode.HALF_UP).toDouble()
}