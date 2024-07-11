package org.shirabox.core.util

import java.math.RoundingMode
import java.security.MessageDigest
import java.text.DecimalFormat

@OptIn(ExperimentalStdlibApi::class)
fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(this.toByteArray())
    return digest.toHexString()
}

fun Double.round(): Double {
    return DecimalFormat("#.##")
        .apply { roundingMode = RoundingMode.HALF_UP }
        .format(this)
        .toDouble()
}