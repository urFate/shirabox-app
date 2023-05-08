package com.tomuki.tomuki.ui.component.top

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun MediaTypesBar(items: List<MediaTypes> = mediaTypesItems){
    val selected = 0

    Row (
        modifier = Modifier
            .wrapContentWidth()
            .padding(16.dp, 0.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEachIndexed { index, mediaType ->
            AssistChip(
                label = { Text(stringResource(mediaType.name)) },
                border = AssistChipDefaults.assistChipBorder(mediaType.color),
                colors = AssistChipDefaults.assistChipColors(
                    if (index == selected) mediaType.color else Color.Transparent,
                    if (index == selected) MaterialTheme.colorScheme.surface
                    else MaterialTheme.colorScheme.inverseSurface),
                onClick = { /*TODO*/ }
            )
        }
    }
}