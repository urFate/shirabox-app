package org.shirabox.app.ui.activity.auth

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.shirabox.app.R
import org.shirabox.app.ui.theme.ShiraBoxTheme
import org.shirabox.core.model.AuthService

class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShiraBoxTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val arguments = intent.extras
                    val activity = (LocalContext.current as? Activity)

                    val authServiceType = arguments?.getString("auth_service")
                        ?.let { AuthService.valueOf(it) }

                    if (authServiceType == null) activity?.finish()

                    when (authServiceType) {
                        AuthService.AnimeSkip -> AnimeSkipAuthForm()
                        else -> Unit
                    }
                }
            }

            enableEdgeToEdge()
        }
    }
}

@Composable
fun AuthForm(
    modifier: Modifier = Modifier,
    logo: @Composable () -> Unit,
    loginFailed: Boolean,
    errorText: String,
    loginInProcess: Boolean,
    onLogin: (email: String, password: String) -> Unit,
    onRegister: () -> Unit,
    onRecover: () -> Unit,
) {
    val emailText = remember { mutableStateOf("") }
    val passwordText = remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Column(
                modifier = Modifier.padding(32.dp, 96.dp, 32.dp, 0.dp),
                verticalArrangement = Arrangement.spacedBy(64.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.login_title),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Medium
                )

                logo()

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth(),
                        value = emailText.value,
                        onValueChange = { emailText.value = it },
                        enabled = !loginInProcess,
                        maxLines = 1,
                        isError = loginFailed,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.AlternateEmail,
                                contentDescription = null
                            )
                        },
                        placeholder = { Text(stringResource(id = R.string.email)) },
                        shape = RoundedCornerShape(26.dp)
                    )
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth(),
                        value = passwordText.value,
                        onValueChange = { passwordText.value = it },
                        enabled = !loginInProcess,
                        maxLines = 1,
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Password,
                                contentDescription = null
                            )
                        },
                        placeholder = { Text(stringResource(id = R.string.password)) },
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(26.dp)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(8.dp, 0.dp)
                                .fillMaxWidth()
                                .clickable { onRecover() },
                            text = stringResource(id = R.string.forgot_password),
                            textAlign = TextAlign.Left,
                            color = MaterialTheme.colorScheme.primary
                        )
                        AnimatedVisibility(
                            visible = loginFailed,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp, 0.dp),
                                textAlign = TextAlign.Left,
                                text = errorText,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom)
            ) {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32),
                    onClick = { onRegister() },
                    enabled = !loginInProcess,
                    contentPadding = PaddingValues(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors()
                        .copy(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        text = stringResource(id = R.string.register)
                    )
                }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(12.dp),
                    enabled = !loginInProcess,
                    onClick = { onLogin(emailText.value, passwordText.value) }
                ) {
                    Text(text = stringResource(id = R.string.login))
                }
            }
        }
    }
}