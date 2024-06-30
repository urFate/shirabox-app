package live.shirabox.shirabox.ui.component.general

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material3.fade
import com.google.accompanist.placeholder.material3.placeholder
import live.shirabox.core.model.Content
import live.shirabox.core.model.ContentType
import live.shirabox.shirabox.R
import live.shirabox.shirabox.ui.theme.light_primary
import live.shirabox.shirabox.ui.theme.mangaPrimary
import live.shirabox.shirabox.ui.theme.ranobePrimary

@Composable
fun ContentCard(
    modifier: Modifier = Modifier,
    item: Content,
    typeBadge: Boolean = false,
    onClick: () -> Unit
) {
    BaseCard(
        modifier = modifier,
        title = item.name,
        image = item.image,
        type = item.type,
        typeBadge = typeBadge
    ) { onClick() }
}

@Composable
fun BaseCard(
    modifier: Modifier,
    title: String,
    image: String,
    type: ContentType,
    typeBadge: Boolean = false,
    onClick: () -> Unit
) {
    var isLoaded by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current

    val request = ImageRequest.Builder(context)
        .data(image)
        .crossfade(true)
        .listener { _, _ ->
            isLoaded = true
        }
        .build()

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10)
    ) {
        Box {
            AsyncImage(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        onClick.invoke()
                    }
                    .placeholder(
                        visible = !isLoaded,
                        highlight = PlaceholderHighlight.fade()
                    ),
                model = request,
                imageLoader = context.imageLoader,
                contentDescription = title,
                contentScale = ContentScale.Crop
            )

            Text(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Transparent,
                            0.5f to Color.Black.copy(alpha = 0.6f),
                            1.0f to Color.Black.copy(alpha = 0.9f)
                        )
                    )
                    .padding(16.dp),
                text = title,
                color = Color.White,
                overflow = TextOverflow.Ellipsis,
                maxLines = 3
            )

            if (typeBadge) {
                val typeData = contentTypeData(type = type)

                Text(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(
                            color = typeData.second,
                            shape = RoundedCornerShape(0.dp, 0.dp, 0.dp, 10.dp)
                        )
                        .padding(8.dp),
                    text = typeData.first,
                    fontSize = 12.sp,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ContentCardPlaceholder(modifier: Modifier) {
    Surface(
        modifier = modifier.then(
            Modifier.placeholder(
                visible = true,
                shape = RoundedCornerShape(10),
                highlight = PlaceholderHighlight.fade()
            )
        ),
        shape = RoundedCornerShape(10)
    ) {}
}

@Composable
fun contentTypeData(type: ContentType): Pair<String, Color> {
    return when (type) {
        ContentType.ANIME -> stringResource(id = R.string.anime) to light_primary
        ContentType.MANGA -> stringResource(id = R.string.manga) to mangaPrimary
        ContentType.RANOBE -> stringResource(id = R.string.ranobe) to ranobePrimary
    }
}