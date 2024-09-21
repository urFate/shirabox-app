package org.shirabox.app.ui.component.general

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp


@Composable
fun SpecialSnackBar(
    icon: @Composable () -> Unit,
    message: String,
    containerColor: Color = Color.Black,
    contentColor: Color = Color.White,
    closeable: Boolean = true,
    clickable: Boolean = true,
    onCloseClick: () -> Unit,
    onClick: () -> Unit = {},
) {

    Snackbar(
        modifier = Modifier
            .height(64.dp)
            .clickable(enabled = clickable, onClick = { onClick() }),
        shape = RoundedCornerShape(8.dp),
        containerColor = containerColor,
        contentColor = contentColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon()
                Text(
                    text = message,
                    color = contentColor,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if(closeable) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    IconButton(onClick = onCloseClick) {
                        Icon(imageVector = Icons.Rounded.Close, tint = contentColor, contentDescription = "Close")
                    }
                }
            }
        }
    }
}