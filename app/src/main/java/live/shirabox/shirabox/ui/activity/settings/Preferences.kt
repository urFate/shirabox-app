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
import androidx.compose.ui.graphics.Color
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
    title: @Composable () -> Unit,
    description: String,
    icon: @Composable () -> Unit = {},
    enabled: Boolean = true,
    dsField: PreferenceField<Boolean>,
    isError: Boolean = false,
    combinedClickable: Boolean = false,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val checkedState =
        AppDataStore.read(context, dsField.key).collectAsState(initial = dsField.defaultValue)
    val checked = remember(checkedState.value) {
        checkedState.value ?: dsField.defaultValue
    }

    Box(
        modifier = Modifier
            .clickable(
                enabled = if (combinedClickable) true else enabled,
                onClickLabel = null,
                role = null,
                onClick = {
                    if (!combinedClickable) {
                        coroutineScope.launch(Dispatchers.IO) {
                            AppDataStore.write(context, dsField.key, !checked)
                        }
                    } else onClick()
                })
            .then(modifier)
    ) {
        Row(
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .padding(16.dp, 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Column(
                modifier = Modifier
                    .padding(16.dp, 0.dp)
                    .weight(1f)
            ) {
                title()
                Text(
                    text = description,
                    color = when (isError) {
                        true -> MaterialTheme.colorScheme.error
                        false -> Color.Unspecified
                    },
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
            Row(
                modifier = Modifier
                    .padding(8.dp, 0.dp)
                    .width(IntrinsicSize.Max),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if(combinedClickable) VerticalDivider(
                    modifier = Modifier.heightIn(8.dp, 48.dp)
                )
                Switch(
                    checked = if(!enabled && isError) false else checked,
                    enabled = enabled,
                    onCheckedChange = {
                        coroutineScope.launch(Dispatchers.IO) {
                            AppDataStore.write(context, dsField.key, !checked)
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