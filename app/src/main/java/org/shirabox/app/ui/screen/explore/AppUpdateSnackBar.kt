package org.shirabox.app.ui.screen.explore

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.onStart
import org.shirabox.app.BuildConfig
import org.shirabox.app.R
import org.shirabox.app.ui.activity.update.AppUpdateActivity
import org.shirabox.app.ui.component.general.SpecialSnackBar
import org.shirabox.data.update.AppUpdateRepository

@Composable
fun AppUpdateSnackbarHost(){
    val context = LocalContext.current
    val snackState = remember { SnackbarHostState() }
    val message = stringResource(id = R.string.update_available)
    var userDismissed by rememberSaveable { mutableStateOf(false) }

    val appUpdateFlowState = AppUpdateRepository.checkAppUpdates(BuildConfig.VERSION_NAME)
        .onStart {
            if (userDismissed) emitAll(emptyFlow())
        }.catch {
            emitAll(emptyFlow())
        }.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(appUpdateFlowState.value) {
        appUpdateFlowState.value?.let {
            if(it.updateAvailable && !userDismissed) {
                snackState.showSnackbar(message = message, duration = SnackbarDuration.Indefinite)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.BottomStart,
    ) {
        SnackbarHost(
            hostState = snackState
        ) { snackbarData: SnackbarData ->
            SpecialSnackBar(
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.RocketLaunch,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        contentDescription = "rocket launch"
                    )
                },
                message = snackbarData.visuals.message,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = {
                    snackbarData.performAction()
                    context.startActivity(Intent(context, AppUpdateActivity::class.java))
                },
                onCloseClick = {
                    userDismissed = true
                    snackbarData.dismiss()
                }
            )
        }
    }
}