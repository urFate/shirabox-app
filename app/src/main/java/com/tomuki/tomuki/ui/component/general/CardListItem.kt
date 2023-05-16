package com.tomuki.tomuki.ui.component.general

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tomuki.tomuki.R

@Composable
fun CardListItem(
    headlineString: String,
    supportingString: String,
    coverImage: ImageBitmap,
    trailingIcon: ImageVector,
    onClick: () -> Unit,
    onTrailingIconClick: () -> Unit ) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(headlineString) },
        supportingContent = { Text(supportingString) },
        trailingContent = {
            Surface(onTrailingIconClick) {
                Icon(
                    trailingIcon, trailingIcon.name,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        leadingContent = {
            Image(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .height(72.dp)
                    .width(54.dp),
                painter = BitmapPainter(coverImage),
                contentDescription = stringResource(id = R.string.history),
                contentScale = ContentScale.Crop)
        }
    )
}

@Composable
fun CardListItem(
    headlineContent: @Composable () -> Unit,
    supportingString: String,
    coverImage: ImageBitmap,
    onClick: () -> Unit ){
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = headlineContent,
        supportingContent = { Text(supportingString) },
        leadingContent = {
            Image(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .height(72.dp)
                    .width(54.dp),
                painter = BitmapPainter(coverImage),
                contentDescription = stringResource(id = R.string.history),
                contentScale = ContentScale.Crop)
        }
    )
}

@Composable
fun CardListItem(
    headlineString: String,
    overlineString: String,
    supportingString: String,
    coverImage: ImageBitmap,
    trailingIcon: ImageVector,
    onTrailingIconClick: () -> Unit,
    onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        overlineContent = { Text(overlineString) },
        headlineContent = { Text(headlineString) },
        supportingContent = { Text(supportingString) },
        trailingContent = {
            Surface(onTrailingIconClick) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = headlineString,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        leadingContent = {
            Image(
                modifier = Modifier
                    .height(40.dp)
                    .width(40.dp)
                    .clip(RoundedCornerShape(100)),
                painter = BitmapPainter(coverImage),
                contentDescription = stringResource(id = R.string.history),
                contentScale = ContentScale.Crop
            )
        }
    )
}
@Composable
fun CardListItem(
    headlineString: String,
    overlineString: String,
    trailingString: String,
    onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        overlineContent = { Text(overlineString) },
        headlineContent = { Text(headlineString) },
        trailingContent = {
            Text(trailingString)
        }
    )
}