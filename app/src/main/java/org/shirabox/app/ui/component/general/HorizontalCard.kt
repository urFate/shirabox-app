package org.shirabox.app.ui.component.general

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun HorizontalCard(
    modifier: Modifier = Modifier,
    title: String,
    subTitle: String,
    image: String,
    onClick: () -> Unit
) {

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10),
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier.clickable { onClick() }
        ) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = image,
                contentDescription = image,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.5f)),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.Start
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Start
                        )
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = subTitle,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        }
    }
}