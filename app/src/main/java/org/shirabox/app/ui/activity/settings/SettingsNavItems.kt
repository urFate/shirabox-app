package org.shirabox.app.ui.activity.settings

import org.shirabox.app.R

sealed class SettingsNavItems(
    val name: Int,
    val description: Int,
    val route: String,
    val icon: Int
) {
    data object Root :
        SettingsNavItems(R.string.title_activity_settings, 0, "root", R.drawable.config)

    data object General : SettingsNavItems(
        R.string.general_settings,
        R.string.general_setting_contents,
        "general",
        R.drawable.config
    )

    data object Appearance : SettingsNavItems(
        R.string.theme_settings,
        R.string.theme_settings_description,
        "appearance",
        R.drawable.swatches
    )

    data object Playback : SettingsNavItems(
        R.string.playback_settings,
        R.string.reader_and_player_settings_description,
        "playback",
        R.drawable.video
    )

    data object About : SettingsNavItems(
        R.string.about_app,
        R.string.about_settings_description,
        "about",
        R.drawable.info_square
    )
}

val settingsNavItems = listOf(
    SettingsNavItems.General,
    SettingsNavItems.Appearance,
    SettingsNavItems.Playback,
    SettingsNavItems.About
)