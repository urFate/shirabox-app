package com.shirabox.shirabox.ui.component.top.navigation

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
import com.shirabox.shirabox.model.ContentType


@Composable
fun MediaNavBar(
    items: List<MediaNavItem> = mediaNavItems,
    activeType: ContentType,
    onClick: (MediaNavItem) -> Unit
) {
    println("CURRENT_BAR: $activeType")

    Row(
        modifier = Modifier
            .wrapContentWidth()
            .padding(16.dp, 0.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach {
            val isSelected = activeType == it.contentType

            AssistChip(
                label = { Text(stringResource(it.name)) },
                border = AssistChipDefaults.assistChipBorder(it.color),
                colors = AssistChipDefaults.assistChipColors(
                    if (isSelected) it.color else Color.Transparent,
                    if (isSelected) MaterialTheme.colorScheme.surface
                    else MaterialTheme.colorScheme.inverseSurface
                ),
                onClick = {
                    onClick(it)
                }
            )
        }
    }
}