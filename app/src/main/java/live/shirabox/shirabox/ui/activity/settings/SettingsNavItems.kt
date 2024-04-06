package live.shirabox.shirabox.ui.activity.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.ui.graphics.vector.ImageVector
import live.shirabox.shirabox.R

sealed class SettingsNavItems(
    val name: Int,
    val description: Int,
    val route: String,
    val icon: ImageVector
) {
    data object Root :
        SettingsNavItems(R.string.title_activity_settings, 0, "root", Icons.Default.Settings)

    data object General : SettingsNavItems(
        R.string.general_settings,
        R.string.general_setting_contents,
        "general",
        Icons.Default.Tune
    )

    data object Appearance : SettingsNavItems(
        R.string.theme_settings,
        R.string.theme_settings_description,
        "appearance",
        Icons.Outlined.Palette
    )

    data object Playback : SettingsNavItems(
        R.string.playback_settings,
        R.string.reader_and_player_settings_description,
        "playback",
        Icons.AutoMirrored.Outlined.PlaylistPlay
    )

    data object About : SettingsNavItems(
        R.string.about_app,
        R.string.about_settings_description,
        "about",
        Icons.Outlined.Info
    )
}

val settingsNavItems = listOf(
    SettingsNavItems.General,
    SettingsNavItems.Appearance,
    SettingsNavItems.Playback,
    SettingsNavItems.About
)