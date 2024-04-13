package live.shirabox.shirabox.ui.activity.settings.category

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import live.shirabox.shirabox.ui.activity.settings.Preference
import live.shirabox.shirabox.ui.activity.settings.settingsNavItems

@Composable
fun SettingsRootScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(settingsNavItems) {
            Preference(
                headlineContent = { Text(stringResource(id = it.name)) },
                supportingContent = {
                    Text(
                        text = stringResource(id = it.description),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = it.icon,
                        contentDescription = it.route,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                navController.navigate(it.route)
            }
        }
    }
}