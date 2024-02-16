package live.shirabox.shirabox.ui.activity.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SwitchPreference(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    description: String,
    icon: @Composable () -> Unit = {},
    enabled: Boolean = true,
    model: SettingsViewModel,
    key: Preferences.Key<Boolean>,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val checked = model.booleanPreferenceFlow(context, key).collectAsState(initial = false)

    Box(
        modifier = Modifier
            .clickable(enabled = enabled, onClickLabel = null, role = null, onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    model.writeBooleanData(context, key, !checked.value)
                }
            })
            .then(modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    title()
                    Text(
                        modifier = Modifier.width(256.dp),
                        text = description,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
                Switch(
                    modifier = Modifier.padding(0.dp, 0.dp),
                    checked = checked.value,
                    onCheckedChange = {
                        coroutineScope.launch(Dispatchers.IO) {
                            model.writeBooleanData(context, key, !checked.value)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun Preference (
    title: String,
    description: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row (
            modifier = Modifier.padding(16.dp, 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Column {
                Text(text = title)
                Text(
                    text = description,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun OptionsBlock(title: String, content: @Composable () -> Unit) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            modifier = Modifier.padding(16.dp, 0.dp),
            text = title,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.inverseSurface
        )
        content()
    }
}