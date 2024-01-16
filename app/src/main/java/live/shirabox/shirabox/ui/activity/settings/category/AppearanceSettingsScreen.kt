package live.shirabox.shirabox.ui.activity.settings.category

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import live.shirabox.shirabox.R
import live.shirabox.shirabox.ui.activity.settings.OptionsBlock
import live.shirabox.shirabox.ui.activity.settings.SettingsScheme
import live.shirabox.shirabox.ui.activity.settings.SettingsViewModel
import live.shirabox.shirabox.ui.activity.settings.SwitchPreference

@Composable
fun AppearanceSettingsScreen(
    viewModel: SettingsViewModel
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        item {
            OptionsBlock(title = stringResource(id = R.string.theme_options_block)) {
                SwitchPreference(
                    title = { Text(text = stringResource(id = R.string.dark_theme_settings)) },
                    description = stringResource(
                        id = R.string.dark_theme_settings_description
                    ),
                    model = viewModel,
                    key = SettingsScheme.FIELD_DARK_MODE
                )
                SwitchPreference(
                    title = { Text(text = stringResource(id = R.string.user_theme_settings)) },
                    description = stringResource(
                        id = R.string.user_theme_settings_description
                    ),
                    enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
                    model = viewModel,
                    key = SettingsScheme.FIELD_USER_COLOR_PALETTE
                )
            }
        }
    }
}