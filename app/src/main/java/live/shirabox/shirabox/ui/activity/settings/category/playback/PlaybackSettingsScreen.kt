package live.shirabox.shirabox.ui.activity.settings.category.playback

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import live.shirabox.shirabox.R
import live.shirabox.shirabox.ui.activity.settings.OptionsBlock
import live.shirabox.shirabox.ui.activity.settings.Preference
import live.shirabox.shirabox.ui.activity.settings.SettingsScheme
import live.shirabox.shirabox.ui.activity.settings.SettingsViewModel
import live.shirabox.shirabox.ui.activity.settings.SwitchPreference

@Composable
fun PlaybackSettingsScreen(
    viewModel: SettingsViewModel
) {
    val qualityVisibilityState = remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Preference(
            title = stringResource(id = R.string.playback_default_quality),
            description = stringResource(id = R.string.playback_default_quality_desc),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.HighQuality,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "quality"
                )
            }
        ) {
            qualityVisibilityState.value = true
        }

        OptionsBlock(title = stringResource(id = R.string.opening_preferences)) {
            SwitchPreference(
                title = { Text(stringResource(id = R.string.opening_skip_preference)) },
                description = stringResource(id = R.string.opening_skip_preference_desc),
                icon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Outlined.SkipNext,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "animeskip"
                    )
                },
                model = viewModel,
                key = SettingsScheme.FIELD_OPENING_SKIP
            )

            Preference(
                title = stringResource(id = R.string.animeskip_preference),
                description = stringResource(id = R.string.animeskip_preference_desc),
                icon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(id = R.drawable.animeskip),
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "animeskip"
                    )
                }
            ) {

            }
        }
    }

    QualityDialog(viewModel, qualityVisibilityState)
}