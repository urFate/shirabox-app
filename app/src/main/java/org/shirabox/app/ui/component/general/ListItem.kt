package org.shirabox.app.ui.component.general

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.CachePolicy
import coil.request.ImageRequest
import org.shirabox.app.R

@Composable
fun ListItem(
    headlineContent: @Composable () -> Unit,
    supportingString: String,
    coverImage: ImageBitmap,
    onClick: () -> Unit
) {
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
                contentScale = ContentScale.Crop
            )
        }
    )
}

@Composable
fun ListItem(
    headlineContent: @Composable () -> Unit,
    supportingString: String,
    coverImage: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val request = ImageRequest.Builder(context)
        .data(coverImage)
        .crossfade(true)
        .memoryCacheKey(coverImage)
        .diskCacheKey(coverImage)
        .diskCachePolicy(CachePolicy.ENABLED)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .build()

    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = headlineContent,
        supportingContent = { Text(supportingString) },
        leadingContent = {
            AsyncImage(
                model = request,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .height(72.dp)
                    .width(54.dp),
                contentDescription = stringResource(id = R.string.history),
                contentScale = ContentScale.Crop
            )
        }
    )
}

@Composable
fun ExtendedListItem(
    modifier: Modifier = Modifier,
    headlineContent: @Composable () -> Unit,
    overlineContent: @Composable () -> Unit = {},
    supportingContent: @Composable () -> Unit = {},
    coverImage: String? = null,
    trailingIcon: ImageVector?,
    clickable: Boolean = true,
    headlineText: String,
    onTrailingIconClick: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    ListItem(
        modifier = if (clickable) Modifier
            .clickable(onClick = onClick)
            .then(modifier) else Modifier.then(modifier),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        overlineContent = overlineContent,
        headlineContent = headlineContent,
        supportingContent = supportingContent,
        trailingContent = {
            trailingIcon?.let {
                IconButton(onClick = onTrailingIconClick) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = "Trailing Icon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        leadingContent = {
            coverImage?.let {
                SubcomposeAsyncImage(
                    modifier = Modifier.height(40.dp).width(40.dp),
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(coverImage)
                        .crossfade(true)
                        .build(),
                    contentScale = ContentScale.Crop,
                    contentDescription = headlineText
                ) {
                    val state = painter.state

                    when(state) {
                        is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent(
                            modifier = Modifier
                                .height(40.dp)
                                .width(40.dp)
                                .clip(RoundedCornerShape(100))
                        )
                        else -> Monogram(str = headlineText)
                    }
                }
            }
        }
    )
}