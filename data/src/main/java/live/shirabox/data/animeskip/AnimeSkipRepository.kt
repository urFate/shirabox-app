package live.shirabox.data.animeskip

import fuel.HttpResponse
import fuel.httpPost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


object AnimeSkipRepository {
    private const val API_ENDPOINT = "https://api.anime-skip.com/graphql"

    /**
     * Used for user authorization only
     */
    private const val APP_CLIENT_ID = "dqQwl07WfgizSfcwpocVvtHEZ9DBFKM7"

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    suspend fun authorize(email: String, md5Hash: String): AuthData {
        return withContext(Dispatchers.IO) {
            async {
                try {
                    val request = clientRequest(RequestBody.loginQuery(email, md5Hash))
                    val response = json.decodeFromString<BaseResponse<LoginData>>(request.body)

                    AuthData(
                        authToken = response.data.login.authToken,
                        refreshToken = response.data.login.refreshToken,
                        account = response.data.login.account
                    )

                } catch (ex: Exception) { throw ex }
            }.await()
        }
    }

    fun reauthorize(refreshToken: String): Flow<AuthData> {
        return flow {
            try {
                val request = clientRequest(RequestBody.loginRefreshQuery(refreshToken))
                val response = json.decodeFromString<BaseResponse<LoginRefresh>>(request.body)

                emit(
                    AuthData(
                        authToken = response.data.loginRefresh.authToken,
                        refreshToken = response.data.loginRefresh.authToken,
                        account = response.data.account
                    )
                )

            } catch (ex: Exception) { throw ex }
        }
    }

    suspend fun createApiClientKey(authToken: String): String {
        return withContext(Dispatchers.IO) {
            async {
                try {
                    val request = API_ENDPOINT.httpPost(
                        headers = mapOf(
                            "content-type" to "application/json",
                            "X-Client-ID" to APP_CLIENT_ID,
                            "Authorization" to "Bearer $authToken"
                        ),
                        body = json.encodeToString(PostWrapper(RequestBody.API_CLIENT_CREATION_MUTATION))
                    )

                    val response = json.decodeFromString<BaseResponse<ClientKeyData>>(request.body)

                    response.data.createApiClient.id

                } catch (ex: Exception) { throw ex }
            }.await()
        }
    }

    suspend fun getExistingClientKey(authToken: String): String? {
        return withContext(Dispatchers.IO) {
            async {
                try {
                    val request = API_ENDPOINT.httpPost(
                        headers = mapOf(
                            "content-type" to "application/json",
                            "X-Client-ID" to APP_CLIENT_ID,
                            "Authorization" to "Bearer $authToken"
                        ),
                        body = json.encodeToString(PostWrapper(RequestBody.API_CLIENT_SEARCH_QUERY))
                    )

                    val response = json.decodeFromString<BaseResponse<ApiClientsData>>(request.body)

                    response.data.myApiClients.ifEmpty { null }?.firstOrNull()?.id

                } catch (ex: Exception) {
                    ex.printStackTrace()
                    return@async null
                }
            }.await()
        }
    }

    suspend fun checkClientKeyValidity(clientKey: String): Flow<Boolean> {
        /**
            On empty query but with valid key server should return 422 code
            Otherwise on invalid key server usually returns 200
         */

        return flow {
            try {
                val request = clientRequest(json.encodeToString(PostWrapper("")), clientKey)
                emit(request.statusCode == 422)
            } catch (ex: Exception) { throw ex }
        }
    }

    fun searchShowId(query: String, clientKey: String): Flow<String?> {
        return flow {
            try {
                val request = clientRequest(RequestBody.searchShowsQuery(query), clientKey)
                val response = json.decodeFromString<BaseResponse<SearchShows>>(request.body)

                emit(response.data.searchShows.firstOrNull()?.id)
            } catch (ex: Exception) { throw ex }
        }
    }

    fun findEpisodeIntroTimestamps(
        showId: String,
        episode: Int,
        season: Int = 1,
        clientKey: String
    ): Flow<Pair<Double, Double>?> {
        return flow {
            try {
                val request =
                    clientRequest(RequestBody.findEpisodesByShowIdQuery(showId), clientKey)
                val response = json.decodeFromString<BaseResponse<FindEpisodes>>(request.body)

                val timestamps = response.data.findEpisodesByShowId.firstOrNull {
                    it.season.toInt() == season && it.number.toInt() == episode
                }?.timestamps

                val introStartTimestamp = timestamps?.firstOrNull {
                    it.type.name == "Intro"
                }
                val introEndTimestamp = timestamps?.indexOf(introStartTimestamp)
                    ?.let { timestamps[it.inc()] }

                if(introStartTimestamp?.at != null && introEndTimestamp?.at != null) {
                    emit(introStartTimestamp.at to introStartTimestamp.at)
                } else emit(null)
            } catch (ex: Exception) { throw ex }
        }
    }


    private suspend fun clientRequest(body: String, clientKey: String = APP_CLIENT_ID): HttpResponse {
        return API_ENDPOINT.httpPost(
            headers = mapOf(
                "content-type" to "application/json",
                "X-Client-ID" to clientKey
            ),
            body = json.encodeToString(PostWrapper(body))
        )
    }
}