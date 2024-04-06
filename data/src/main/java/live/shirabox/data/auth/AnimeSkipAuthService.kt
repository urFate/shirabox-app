package live.shirabox.data.auth

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import live.shirabox.core.datastore.AppDataStore
import live.shirabox.core.datastore.DataStoreScheme
import live.shirabox.core.util.md5
import live.shirabox.data.animeskip.AnimeSkipRepository

object AnimeSkipAuthService : AbstractAuthService(
    registrationUrl = "https://anime-skip.com/sign-up",
    accessRecoverUrl = "https://anime-skip.com/forgot-password",
    logoUrl = "https://anime-skip.com/_nuxt/logo-nav.49e247d2.svg"
) {
    override suspend fun login(context: Context, email: String, password: String): Flow<Boolean> {
        return flow {
            /**
             * Firstly try to find existing ShiraBox client key
             * If it is not exists create new one
             */

            try {
                val authData = AnimeSkipRepository.authorize(email = email, md5Hash = password.md5())

                val clientKey = withContext(Dispatchers.IO) {
                    async {
                        val token = authData.authToken

                        when(val existingKey = AnimeSkipRepository.getExistingClientKey(token)) {
                            null -> AnimeSkipRepository.createApiClientKey(token)
                            else -> existingKey
                        }

                    }
                }.await()

                AppDataStore.write(context, DataStoreScheme.FIELD_ANIMESKIP_USER_CLIENT_ID, clientKey)
                emit(true)
            } catch (ex: Exception) { throw ex }
        }
    }
}