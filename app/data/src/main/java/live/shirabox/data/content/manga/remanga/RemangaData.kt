package live.shirabox.data.content.manga.remanga

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookData(
    @SerialName("id") val id: Int,
    @SerialName("img") val img: ImgData,
    @SerialName("en_name") val enName: String,
    @SerialName("rus_name") val rusName: String,
    @SerialName("another_name") val anotherName: String,
    @SerialName("dir") val dir: String,
    @SerialName("description") val description: String,
    @SerialName("issue_year") val issueYear: Int,
    @SerialName("avg_rating") val avgRating: String,
    @SerialName("status") val status: StatusData,
    @SerialName("genres") val genres: List<GenresData>?,
    @SerialName("branches") val branches: List<BranchesData>,
    @SerialName("count_chapters") val countChapters: Int,
    @SerialName("is_licensed") val isLicensed: Boolean?,
    @SerialName("is_yaoi") val isYaoi: Boolean,
    @SerialName("is_erotic") val isErotic: Boolean
)

@Serializable
data class LibraryBookData (
    var id: Int,
    @SerialName("en_name") var enName: String,
    @SerialName("rus_name") var rusName: String,
    @SerialName("dir") var dir: String,
    @SerialName("issue_year") var issueYear: Int,
    @SerialName("avg_rating") var avgRating: String,
    @SerialName("is_yaoi") var isYaoi: Boolean,
    @SerialName("is_erotic") var isErotic: Boolean,
    @SerialName("genres") var genres: List<GenresData>?,
    @SerialName("img") var img: ImgData,
)

@Serializable
data class SearchBookData (
    var id: Int,
    @SerialName("en_name") var enName: String,
    @SerialName("rus_name") var rusName: String,
    @SerialName("dir") var dir: String,
    @SerialName("issue_year") var issueYear: Int?,
    @SerialName("avg_rating") var avgRating: String,
    @SerialName("is_yaoi") var isYaoi: Boolean,
    @SerialName("is_erotic") var isErotic: Boolean,
    @SerialName("img") var img: ImgData,
)

@Serializable
data class ChapterData (
    @SerialName("id") val id: Int,
    @SerialName("index") val index: Int,
    @SerialName("tome") val volume: Int,
    @SerialName("chapter") val chapter: String,
    @SerialName("name") val name: String,
    @SerialName("upload_date") val uploadDate: String,
)

@Serializable
data class ChapterPagesData(
    @SerialName("id") val id: Int,
    @SerialName("tome") val volume: Int,
    @SerialName("chapter") val chapter: String,
    @SerialName("name") val name: String,
    @SerialName("upload_date") val uploadDate: String,
    @SerialName("title_id") val titleId: Int,
    @SerialName("branch_id") val branchId: Int,
    @SerialName("index") val index: Int,
    @SerialName("pages") val pages: ArrayList<ArrayList<PageData>>
)

@Serializable
data class PageData (
    @SerialName("id") val id: Int,
    @SerialName("link") val img: String,
    @SerialName("height") val height: Int,
    @SerialName("width") val width: Int,
)

@Serializable
data class GenresData (
    var id: Int,
    var name: String
)

@Serializable
data class ImgData (
    var high: String,
    var mid: String,
    var low: String
)

@Serializable
data class StatusData (
    val id: Int,
    val name: String
)

@Serializable
data class BranchesData(
    val id: Long,
    @SerialName("count_chapters") val countChapters: Int
)

@Serializable
data class LibraryWrapperData<T>(
    val content: List<T>,
)

@Serializable
data class WrapperData<T>(
    val content: T
)