package org.shirabox.app.ui.activity.settings.category

import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import org.shirabox.app.R
import org.shirabox.app.ui.activity.settings.OptionsBlock
import org.shirabox.app.ui.activity.settings.SwitchPreference
import org.shirabox.core.datastore.DataStoreScheme

@Composable
fun AppearanceSettingsScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        item {
            OptionsBlock(title = stringResource(id = R.string.theme_options_block)) {
                SwitchPreference(
                    headlineContent = {
                        Text(text = stringResource(id = R.string.dark_theme_settings))
                    },
                    supportingContent = {
                        Text(
                            text = stringResource(id = R.string.dark_theme_settings_description),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    },
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.moon_star),
                            contentDescription = "dark mode",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    dsField = DataStoreScheme.FIELD_DARK_MODE
                )

                val enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                val textColor = when(enabled) {
                    true -> Color.Unspecified
                    false -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
                val iconColor = when(enabled) {
                    true -> MaterialTheme.colorScheme.primary
                    false -> MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
                }

                SwitchPreference(
                    headlineContent = {
                        Text(
                            text = stringResource(id = R.string.user_theme_settings),
                            color = textColor
                        )
                    },
                    supportingContent = {
                        Text(
                            text = stringResource(id = R.string.user_theme_settings_description),
                            color = textColor,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    },
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.swatches),
                            contentDescription = "dynamic color",
                            tint = iconColor
                        )
                    },
                    enabled = enabled,
                    uncheckSwitch = !enabled,
                    dsField = DataStoreScheme.FIELD_DYNAMIC_COLOR
                )
            }
        }
    }
}