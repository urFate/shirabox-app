package live.shirabox.shirabox.ui.activity.update

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.halilibo.richtext.commonmark.CommonmarkAstNodeParser
import com.halilibo.richtext.markdown.BasicMarkdown
import com.halilibo.richtext.ui.material3.RichText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import live.shirabox.core.update.UpdateManager
import live.shirabox.data.update.AppUpdateRepository
import live.shirabox.data.update.AppUpdateState
import live.shirabox.shirabox.BuildConfig
import live.shirabox.shirabox.R
import live.shirabox.shirabox.ui.theme.ShiraBoxTheme
import java.io.File
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AppUpdateActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShiraBoxTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val activity = context as Activity

                    var isLoading by remember {
                        mutableStateOf(true)
                    }

                    val appUpdateFlowState =
                        AppUpdateRepository.checkAppUpdates(BuildConfig.VERSION_NAME)
                            .catch {
                                emitAll(emptyFlow())
                                it.printStackTrace()
                                Toast.makeText(context, it.stackTraceToString(), Toast.LENGTH_LONG)
                                    .show()
                                activity.finish()
                            }.onCompletion {
                                isLoading = false
                            }.collectAsStateWithLifecycle(initialValue = null)

                    when (isLoading) {
                        true -> LoadingScreen()
                        false -> appUpdateFlowState.value?.let {
                            AppUpdateScreen(it)
                        }
                    }
                }
            }

            enableEdgeToEdge()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUpdateScreen(
    appUpdateState: AppUpdateState
) {
    val context = LocalContext.current
    val activity = context as Activity
    val isUpdating = remember { mutableStateOf(false) }
    val dialogVisibilityState = remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { activity.finish() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Finish",
                        )
                    }
                },
            )
        },
        bottomBar = {
            if (appUpdateState.updateAvailable) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 96.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        modifier = Modifier.size(256.dp, 64.dp),
                        onClick = {
                            isUpdating.value = true
                            dialogVisibilityState.value = true
                        },
                        enabled = !isUpdating.value
                    ) {
                        Text(text = stringResource(id = R.string.update_install))
                    }
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp, 0.dp)
                .padding(it),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(64.dp),
                    imageVector = when (appUpdateState.updateAvailable) {
                        true -> Icons.Outlined.RocketLaunch
                        false -> Icons.Outlined.Verified
                    },
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "rocket"
                )
            }
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = when (appUpdateState.updateAvailable) {
                    true -> stringResource(id = R.string.update)
                    false -> stringResource(id = R.string.no_update_required)
                },
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                fontSize = 28.sp
            )

            HorizontalDivider(
                modifier = Modifier.widthIn(32.dp, 256.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                RichText {
                    val parser = remember { CommonmarkAstNodeParser() }
                    val astNode = remember(parser) { parser.parse(appUpdateState.release.notes) }

                    BasicMarkdown(astNode = astNode)
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    val dateString = formatter.format(Date(appUpdateState.release.createdAt))

                    Text(
                        text = appUpdateState.release.tag,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.7f)
                    )
                    Text(
                        text = stringResource(id = R.string.update_build_date, dateString),
                        color = MaterialTheme.colorScheme.onBackground.copy(0.7f)
                    )
                }
            }
        }
    }

    UpdateDialog(
        dialogVisibilityState = dialogVisibilityState,
        updateProcessState = isUpdating,
        appUpdateState = appUpdateState
    )
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun UpdateDialog(
    dialogVisibilityState: MutableState<Boolean>,
    updateProcessState: MutableState<Boolean>,
    appUpdateState: AppUpdateState
) {
    val context = LocalContext.current
    val activity = context as Activity
    var progress by remember(dialogVisibilityState) { mutableFloatStateOf(0.0F) }
    var downloadFinished by remember(progress) { mutableStateOf(false) }
    var downloadError by remember { mutableStateOf(false) }

    val cacheDir = context.cacheDir

    LaunchedEffect(dialogVisibilityState.value) {
        if (dialogVisibilityState.value) {
            launch(Dispatchers.IO) {
                val packageFile = File(cacheDir, "sb-${appUpdateState.release.tag}.apk")
                downloadFinished = false

                UpdateManager.downloadFile(
                    url = URL(appUpdateState.release.downloadUrl),
                    file = packageFile,
                    onProgress = {
                        progress = it
                    },
                    onFinish = {
                        downloadFinished = true

                        when(it) {
                            is Exception -> {
                                downloadError = true
                                progress = 1.0f
                            }
                            else -> {
                                UpdateManager.installPackage(
                                    context,
                                    BuildConfig.APPLICATION_ID,
                                    packageFile
                                )
                                activity.finish()
                            }
                        }
                    }
                )
            }
        }
    }

    AnimatedVisibility(visible = dialogVisibilityState.value) {
        AlertDialog(
            onDismissRequest = {
                UpdateManager.cancelDownload()

                downloadFinished = true
                updateProcessState.value = false
                dialogVisibilityState.value = false
                progress = 0.0F
            },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        UpdateManager.cancelDownload()

                        downloadFinished = true
                        updateProcessState.value = false
                        dialogVisibilityState.value = false
                        progress = 0.0F
                    },
                    enabled = !downloadFinished
                ) {
                    Text(text = stringResource(id = R.string.cancel_installation))
                }
            },
            icon = {
                Icon(
                    modifier = Modifier.size(48.dp),
                    imageVector = Icons.Rounded.Downloading,
                    contentDescription = "Downloading",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(text = stringResource(id = R.string.update_dialog_title))
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = when (downloadError) {
                            true -> stringResource(id = R.string.download_error)
                            false -> stringResource(id = R.string.file_downloading)
                        },
                        color = when(downloadError) {
                            true -> MaterialTheme.colorScheme.error
                            false -> MaterialTheme.colorScheme.onBackground.copy(0.7f)
                        }
                    )
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        progress = { progress },
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        )
    }
}