package org.shirabox.core.util

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalStdlibApi::class)
fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(this.toByteArray())
    return digest.toHexString()
}

fun Long.getDuration(): String {
    val date = Date(this)
    val formatter = SimpleDateFormat("H:mm", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(date)
}

fun Double.round(): Double = Math.round(this * 100.0) / 100.0