package org.shirabox.core.media

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL

object HlsParser {
    private const val START_KEY = "#EXTM3U"
    private const val END_KEY  = "#EXT-X-ENDLIST"
    private const val SEGMENT_KEY = "#EXTINF"

    fun parseUrl(url: String): List<String> {
        if (url.isEmpty()) throw IllegalArgumentException("Specified URL is empty.")

        val mUrl = URL(url)
        val inputStream = mUrl.openStream()

        val urls = readSegments(inputStream, url.substring(0, url.lastIndexOf('/').inc()))

        if (urls.isEmpty()) throw IllegalStateException("No URL(s) found in the provided file !")

        return urls
    }

    private fun readSegments(inputStream: InputStream, vararg query: String): List<String> {
        val lines = readLines(inputStream)

        if (lines.isEmpty()) throw IllegalArgumentException("File is empty.")

        if (lines[0] != START_KEY)
            throw IllegalArgumentException("Start key is not found. Is this M3U8 file?")

        if (!lines.contains(END_KEY))
            throw IllegalArgumentException("End key is not found. Is this M3U8 file?")

        val result = mutableListOf<String>()

        var iterator = 0

        while (iterator < lines.size) {
            val line = lines[iterator]

            if (line.startsWith(SEGMENT_KEY)) {
                var split = line.split(",").toTypedArray()

                if (split.size == 1 || split[1].trim().isEmpty()) {

                    var nextLine = lines[iterator.inc()].trim()

                    if (nextLine.startsWith(SEGMENT_KEY)) {
                        iterator--
                        continue
                    }

                    split = nextLine.split(",").toTypedArray()

                    if (split.size == 2) nextLine = split[1].trim()

                    if (!nextLine.startsWith("http")) {
                        if (query.isNotEmpty()) nextLine = query[0] + nextLine
                        else throw java.lang.IllegalArgumentException("The url doesn't start with 'http' and there is no partial url provided !")
                    }

                    result.add(nextLine)
                }
            }

            iterator += 1
        }

        return result
    }

    private fun readLines(inputStream: InputStream): List<String> {
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))

        return bufferedReader.readLines()
    }
}