package org.shirabox.app.ui.activity.downloads.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun DownloadsTab(text: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    Tab(
        selected = selected,
        unselectedContentColor = MaterialTheme.colorScheme.outline,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(2.dp, 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = icon,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                contentDescription = "downloads"
            )

            Text(
                text = text,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}