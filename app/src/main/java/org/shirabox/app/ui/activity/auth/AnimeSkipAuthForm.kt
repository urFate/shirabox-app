package org.shirabox.app.ui.activity.auth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.decode.SvgDecoder
import kotlinx.coroutines.flow.catch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import org.shirabox.app.R
import org.shirabox.core.datastore.AppDataStore
import org.shirabox.core.datastore.DataStoreScheme
import org.shirabox.data.auth.AbstractAuthService
import org.shirabox.data.auth.AnimeSkipAuthService
import java.io.IOException

@OptIn(ExperimentalSerializationApi::class)
@Composable
fun AnimeSkipAuthForm(authService: AbstractAuthService = AnimeSkipAuthService) {
    val context = LocalContext.current
    val activity = (LocalContext.current as? Activity)

    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val loginFailed: MutableState<Boolean?> = remember { mutableStateOf(null) }
    val loginInProcess = remember { mutableStateOf(false) }
    val errorText = remember { mutableStateOf("") }

    LaunchedEffect(emailState.value, passwordState.value) {
        if(emailState.value.isNotEmpty() && passwordState.value.isNotEmpty()) {
            loginInProcess.value = true

            authService.login(context, emailState.value, passwordState.value).catch {
                it.printStackTrace()
                errorText.value = when(it) {
                    is MissingFieldException -> context.resources.getString(R.string.invalid_credentials)
                    is IOException -> context.resources.getString(R.string.no_internet_connection)
                    else -> context.resources.getString(R.string.no_contents)
                }
                emit(false)
            }.collect {
                loginFailed.value = !it
                loginInProcess.value = false
                emailState.value = ""
                passwordState.value = ""
            }
        }
    }

    LaunchedEffect(loginInProcess.value) {
        loginFailed.value?.let {
            if(!it) {
                AppDataStore.write(
                    context,
                    DataStoreScheme.FIELD_USE_ANIMESKIP.key,
                    true
                )
                activity?.finish()
            }
        }
    }

    AuthForm(
        logo = {
            SubcomposeAsyncImage(
                modifier = Modifier.width(92.dp),
                loading = {
                    CircularProgressIndicator(
                        strokeCap = StrokeCap.Round
                    )
                },
                imageLoader = ImageLoader.Builder(context)
                    .components { add(SvgDecoder.Factory()) }
                    .build(),
                model = authService.logoUrl,
                contentDescription = "logo",
                contentScale = ContentScale.Fit
            )
        },
        loginFailed = loginFailed.value ?: false,
        loginInProcess = loginInProcess.value,
        errorText = errorText.value,
        onLogin = { email, password ->
            loginFailed.value = null
            emailState.value = email
            passwordState.value = password
        },
        onRegister = {
            ContextCompat.startActivity(
                context,
                Intent(Intent.ACTION_VIEW).setData(Uri.parse(authService.registrationUrl)),
                null
            )
        },
        onRecover = {
            ContextCompat.startActivity(
                context,
                Intent(Intent.ACTION_VIEW).setData(Uri.parse(authService.accessRecoverUrl)),
                null
            )
        }
    )
}