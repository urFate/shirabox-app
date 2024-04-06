package live.shirabox.data.auth

import android.content.Context
import kotlinx.coroutines.flow.Flow

abstract class AbstractAuthService(
    val registrationUrl: String,
    val accessRecoverUrl: String,
    val logoUrl: String
) {
    abstract suspend fun login(context: Context, email: String, password: String): Flow<Boolean>
}