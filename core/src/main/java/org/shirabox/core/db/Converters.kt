package org.shirabox.core.db

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.shirabox.core.model.ContentType
import org.shirabox.core.model.Quality


class Converters {

    @TypeConverter
    fun fromContentType(contentType: ContentType): String {
        return contentType.name
    }

    @TypeConverter
    fun toContentType(contentType: String): ContentType {
        return ContentType.valueOf(contentType)
    }

    @TypeConverter
    fun fromQuality(quality: Quality): String {
        return quality.name
    }

    @TypeConverter
    fun toQuality(quality: String): Quality {
        return Quality.valueOf(quality)
    }

    @TypeConverter
    fun decodeList(listOfString: String): List<String>? {
        return Json.decodeFromString(listOfString)
    }

    @TypeConverter
    fun encodeList(listOfString: List<String>?): String {
        return Json.encodeToString(listOfString)
    }

    @TypeConverter
    fun encodeScores(scores: Map<Int, Int>): String {
        return Json.encodeToString(scores)
    }

    @TypeConverter
    fun decodeScores(scores: String): Map<Int, Int> {
        return Json.decodeFromString(scores)
    }

    @TypeConverter
    fun encodeVideos(videos: Map<Quality, String>?): String {
        return Json.encodeToString(videos)
    }

    @TypeConverter
    fun decodeVideos(videosData: String): Map<Quality, String>? {
        return Json.decodeFromString(videosData)
    }

    @TypeConverter
    fun encodeVideoMarkers(videoMarkers: Pair<Long, Long>): String {
        return Json.encodeToString(videoMarkers)
    }

    @TypeConverter
    fun decodeVideoMarkers(videoMarkers: String): Pair<Long, Long> {
        return Json.decodeFromString(videoMarkers)
    }
}