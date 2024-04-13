package live.shirabox.shirabox.ui.activity.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import live.shirabox.core.datastore.AppDataStore
import live.shirabox.core.datastore.PreferenceField

@Composable
fun SwitchPreference(
    modifier: Modifier = Modifier,
    headlineContent: @Composable () -> Unit,
    supportingContent: @Composable () -> Unit,
    leadingContent: @Composable () -> Unit = {},
    enabled: Boolean = true,
    uncheckSwitch: Boolean = false,
    dsField: PreferenceField<Boolean>
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val checkedState =
        AppDataStore.read(context, dsField.key).collectAsState(initial = dsField.defaultValue)
    val checked = remember(checkedState.value) {
        checkedState.value ?: dsField.defaultValue
    }

    Preference(
        modifier = modifier,
        headlineContent = headlineContent,
        supportingContent = supportingContent,
        leadingContent = leadingContent,
        trailingContent = {
            Switch(
                checked = if(uncheckSwitch) false else checked,
                enabled = enabled,
                onCheckedChange = {
                    coroutineScope.launch(Dispatchers.IO) {
                        AppDataStore.write(context, dsField.key, !checked)
                    }
                }
            )
        },
        enabled = enabled
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            AppDataStore.write(context, dsField.key, !checked)
        }
    }
}

@Composable
fun CombinedSwitchPreference(
    modifier: Modifier = Modifier,
    headlineContent: @Composable () -> Unit,
    supportingContent: @Composable () -> Unit,
    leadingContent : @Composable () -> Unit = {},
    enabled: Boolean = true,
    switchEnabled: Boolean = true,
    uncheckSwitch: Boolean = false,
    dsField: PreferenceField<Boolean>,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val checkedState =
        AppDataStore.read(context, dsField.key).collectAsState(initial = dsField.defaultValue)
    val checked = remember(checkedState.value) {
        checkedState.value ?: dsField.defaultValue
    }

    Preference(
        modifier = modifier,
        headlineContent = headlineContent,
        supportingContent = supportingContent,
        leadingContent = leadingContent,
        trailingContent = {
            VerticalDivider(
                modifier = Modifier.heightIn(8.dp, 48.dp)
            )
            Switch(
                checked = if(uncheckSwitch) false else checked,
                enabled = switchEnabled,
                onCheckedChange = {
                    coroutineScope.launch(Dispatchers.IO) {
                        AppDataStore.write(context, dsField.key, !checked)
                    }
                }
            )
        },
        enabled = enabled,
        onClick = onClick
    )
}

@Composable
fun Preference (
    modifier: Modifier = Modifier,
    headlineContent: @Composable () -> Unit,
    supportingContent: @Composable () -> Unit,
    leadingContent: @Composable () -> Unit = {},
    trailingContent: @Composable () -> Unit = {},
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable(
                enabled = enabled,
                onClickLabel = null,
                role = null,
                onClick = onClick
            )
            .then(modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .width(IntrinsicSize.Max)
                .padding(16.dp, 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            leadingContent()
            Column(
                modifier = Modifier.weight(1f)
            ) {
                headlineContent()
                supportingContent()
            }
            Row(
                modifier = Modifier
                    .padding(8.dp, 0.dp)
                    .width(IntrinsicSize.Max),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                trailingContent()
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