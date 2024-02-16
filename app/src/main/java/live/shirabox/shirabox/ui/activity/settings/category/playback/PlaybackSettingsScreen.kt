package live.shirabox.shirabox.ui.activity.settings.category.playback

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import live.shirabox.shirabox.R
import live.shirabox.shirabox.ui.activity.settings.Preference
import live.shirabox.shirabox.ui.activity.settings.SettingsViewModel

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
            }) {
            qualityVisibilityState.value = true
        }
    }

    QualityDialog(viewModel, qualityVisibilityState)
}