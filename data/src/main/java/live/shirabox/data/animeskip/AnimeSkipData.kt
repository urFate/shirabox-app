package live.shirabox.data.animeskip

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(
    @SerialName("data")
    val data: T
)

@Serializable
data class LoginData(
    @SerialName("login")
    val login: Login,
    @SerialName("account")
    val account: AccountData
)

@Serializable
data class AccountData(
    @SerialName("account")
    val account: AnimeSkipAccount
)

@Serializable
data class AnimeSkipAccount(
    @SerialName("email")
    val email: String,
    @SerialName("username")
    val username: String
)

@Serializable
data class Login(
    @SerialName("authToken")
    val authToken: String,
    @SerialName("refreshToken")
    val refreshToken: String
)

@Serializable
data class ClientKeyData(
    @SerialName("createApiClient")
    val createApiClient: CreateApiClient
)

@Serializable
data class CreateApiClient(
    @SerialName("appName")
    val appName: String,
    @SerialName("id")
    val id: String
)

@Serializable
data class ApiClientsData(
    @SerialName("myApiClients")
    val myApiClients: List<MyApiClient>
)

@Serializable
data class MyApiClient(
    @SerialName("id")
    val id: String
)

@Serializable
data class LoginRefresh(
    @SerialName("loginRefresh")
    val loginRefresh: Login,
    @SerialName("account")
    val account: AccountData
)

@Serializable
data class SearchShows(
    @SerialName("searchShows")
    val searchShows: List<SearchShow>
)

@Serializable
data class SearchShow(
    @SerialName("id")
    val id: String
)

@Serializable
data class FindEpisodes(
    @SerialName("findEpisodesByShowId")
    val findEpisodesByShowId: List<FindEpisodesByShowId>
)

@Serializable
data class FindEpisodesByShowId(
    @SerialName("absoluteNumber")
    val absoluteNumber: String,
    @SerialName("number")
    val number: String,
    @SerialName("season")
    val season: String,
    @SerialName("timestamps")
    val timestamps: List<Timestamp>
)

@Serializable
data class Timestamp(
    @SerialName("at")
    val at: Double,
    @SerialName("type")
    val type: Type
)

@Serializable
data class Type(
    @SerialName("name")
    val name: String
)