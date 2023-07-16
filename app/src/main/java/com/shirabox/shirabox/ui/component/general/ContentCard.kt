package com.shirabox.shirabox.ui.component.general

import android.content.Intent
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material3.fade
import com.google.accompanist.placeholder.material3.placeholder
import com.shirabox.shirabox.model.Content
import com.shirabox.shirabox.ui.activity.resource.ResourceActivity

@Composable
fun ContentCard(modifier: Modifier = Modifier, item: Content) {
    val context = LocalContext.current

    var isLoaded by remember {
        mutableStateOf(false)
    }

    val request = ImageRequest.Builder(LocalContext.current)
        .data(item.coverUri)
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
                        context.startActivity(
                            Intent(
                                context,
                                ResourceActivity::class.java
                            )
                        )
                    }
                    .placeholder(
                        visible = !isLoaded,
                        highlight = PlaceholderHighlight.fade()
                    ),
                model = request,
                contentDescription = item.altName,
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
                text = item.name,
                color = Color.White,
                overflow = TextOverflow.Ellipsis,
                maxLines = 3
            )
        }
    }
}