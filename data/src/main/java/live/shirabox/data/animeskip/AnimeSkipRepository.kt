package live.shirabox.data.animeskip

import fuel.HttpResponse
import fuel.httpPost
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

import kotlinx.serialization.json.Json


object AnimeSkipRepository {
    private const val API_ENDPOINT = "https://api.anime-skip.com/graphql"

    /**
     * Used for user authorization only
     */
    private const val APP_CLIENT_ID = "dqQwl07WfgizSfcwpocVvtHEZ9DBFKM7"

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    fun authorize(email: String, md5Hash: String): Flow<AuthData?> {
        return flow {
            try {
                val request = clientRequest(RequestBody.loginQuery(email, md5Hash))
                val response = json.decodeFromString<BaseResponse<LoginData>>(request.body)

                emit(
                    AuthData(
                        authToken = response.data.login.authToken,
                        refreshToken = response.data.login.refreshToken,
                        account = response.data.account.account
                    )
                )

            } catch (_: Exception) { emit(null) }
        }
    }

    fun reauthorize(refreshToken: String): Flow<AuthData?> {
        return flow {
            try {
                val request = clientRequest(RequestBody.loginRefreshQuery(refreshToken))
                val response = json.decodeFromString<BaseResponse<LoginRefresh>>(request.body)

                emit(
                    AuthData(
                        authToken = response.data.loginRefresh.authToken,
                        refreshToken = response.data.loginRefresh.authToken,
                        account = response.data.account.account
                    )
                )

            } catch (_: Exception) { emit(null) }
        }
    }

    fun createApiClientKey(authToken: String): Flow<String?> {
        return flow {
            try {
                val request = API_ENDPOINT.httpPost(
                    headers = mapOf(
                        "content-type" to "application/json",
                        "X-Client-ID" to APP_CLIENT_ID,
                        "Authorization" to "Bearer $authToken"
                    ),
                    body = RequestBody.API_CLIENT_CREATION_MUTATION
                )

                val response = json.decodeFromString<BaseResponse<ClientKeyData>>(request.body)

                emit(response.data.createApiClient.id)

            } catch (_: Exception) { emit(null) }
        }
    }

    fun getExistingClientKey(authToken: String): Flow<String?> {
        return flow {
            try {
                val request = API_ENDPOINT.httpPost(
                    headers = mapOf(
                        "content-type" to "application/json",
                        "X-Client-ID" to APP_CLIENT_ID,
                        "Authorization" to "Bearer $authToken"
                    ),
                    body = RequestBody.API_CLIENT_SEARCH_QUERY
                )

                val response = json.decodeFromString<BaseResponse<ApiClientsData>>(request.body)

                emit(response.data.myApiClients.firstOrNull()?.id)

            } catch (_: Exception) { emit(null) }
        }
    }

    fun searchShowId(query: String, clientKey: String): Flow<String?> {
        return flow {
            try {
                val request = clientRequest(RequestBody.searchShowsQuery(query), clientKey)
                val response = json.decodeFromString<BaseResponse<SearchShows>>(request.body)

                emit(response.data.searchShows.firstOrNull()?.id)
            } catch (_: Exception) { emit(null) }
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
            } catch (_: Exception) {
                emit(null)
            }
        }
    }


    private suspend fun clientRequest(body: String, clientKey: String = APP_CLIENT_ID): HttpResponse {
        return API_ENDPOINT.httpPost(
            headers = mapOf(
                "content-type" to "application/json",
                "X-Client-ID" to clientKey
            ),
            body = body
        )
    }
}