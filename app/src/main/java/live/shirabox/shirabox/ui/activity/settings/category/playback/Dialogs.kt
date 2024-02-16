package live.shirabox.shirabox.ui.activity.settings.category.playback

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Hd
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.Sd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import live.shirabox.shirabox.R
import live.shirabox.shirabox.ui.activity.settings.SettingsScheme
import live.shirabox.shirabox.ui.activity.settings.SettingsViewModel

@Composable
fun QualityDialog(viewModel: SettingsViewModel, visibilityState: MutableState<Boolean>) {
    val context = LocalContext.current
    val currentQualityFlowState =
        viewModel.intPreferenceFlow(context, SettingsScheme.FIELD_DEFAULT_QUALITY).collectAsState(
            initial = 0
        )
    val coroutineScope = rememberCoroutineScope()

    if(visibilityState.value) {
        AlertDialog(
            onDismissRequest = {
                visibilityState.value = false
            },
            confirmButton = {
                TextButton(onClick = { visibilityState.value = false }) {
                    Text(text = stringResource(id = R.string.confirm))
                }
            },
            icon = {
                Icon(
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary,
                    imageVector = Icons.Outlined.HighQuality,
                    contentDescription = "quality"
                )
            },
            title = { Text(stringResource(id = R.string.playback_default_quality)) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = stringResource(id = R.string.playback_default_quality_desc))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        content = {
                            QualityListItem(
                                text = stringResource(id = R.string.high_quality),
                                description = "1080p",
                                icon = Icons.Outlined.HighQuality,
                                selected = currentQualityFlowState.value == 1080
                            ) {
                                coroutineScope.launch {
                                    viewModel.writeIntData(
                                        context,
                                        SettingsScheme.FIELD_DEFAULT_QUALITY,
                                        1080
                                    )
                                }
                            }

                            QualityListItem(
                                text = stringResource(id = R.string.medium_quality),
                                description = "720p",
                                icon = Icons.Outlined.Hd,
                                selected = currentQualityFlowState.value == 720
                            ) {
                                coroutineScope.launch {
                                    viewModel.writeIntData(
                                        context,
                                        SettingsScheme.FIELD_DEFAULT_QUALITY,
                                        720
                                    )
                                }
                            }

                            QualityListItem(
                                text = stringResource(id = R.string.low_quality),
                                description = "480p",
                                icon = Icons.Outlined.Sd,
                                selected = currentQualityFlowState.value == 480
                            ) {
                                coroutineScope.launch {
                                    viewModel.writeIntData(
                                        context,
                                        SettingsScheme.FIELD_DEFAULT_QUALITY,
                                        480
                                    )
                                }
                            }
                        }
                    )
                }
            }
        )
    }
}

@Composable
private fun QualityListItem(
    text: String,
    description: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = {
            Text(
                text = text,
                fontSize = 15.sp
            )
        },
        supportingContent = { Text(description) },
        leadingContent = {
            Icon(
                tint = MaterialTheme.colorScheme.primary,
                imageVector = icon,
                contentDescription = description
            )
        },
        trailingContent = {
            RadioButton(
                selected = selected,
                onClick = { onClick() })
        }
    )
}