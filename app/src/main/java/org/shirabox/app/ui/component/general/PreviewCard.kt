package org.shirabox.app.ui.component.general

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.shirabox.app.R
import org.shirabox.core.model.ContentKind
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun PreviewCard(
    modifier: Modifier = Modifier,
    title: String,
    team: String,
    episode: Int,
    kind: ContentKind,
    imagePath: String,
    watchingTime: Long,
    streamLength: Long,
    onClick: () -> Unit
    ) {
    val remainingTime = streamLength.minus(watchingTime)
    val formattingPattern = if(remainingTime >= 3600000L) "HH:mm:ss" else "mm:ss"
    val dateFormat = SimpleDateFormat(formattingPattern, Locale.getDefault())
    val seenProgress = watchingTime.toFloat().div(streamLength)
    val remainingTimeLabel = dateFormat.format(Date(remainingTime))

    var barSize by remember { mutableStateOf(IntSize.Zero) }

    Surface(
        modifier = modifier,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(10)
    ) {
        Box(
            modifier = Modifier.clickable { onClick() }
        ) {
            AsyncImage(
                modifier = Modifier.fillMaxSize().blur(3.dp),
                model = imagePath,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.5f)),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = title,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End
                    )
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = team,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = when (kind) {
                                    ContentKind.MOVIE -> stringResource(id = R.string.kind_movie)
                                    else -> stringResource(id = R.string.episode_string, episode)
                                },
                                color = Color.White,
                                textAlign = TextAlign.End
                            )
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(id = R.string.remaining, remainingTimeLabel),
                                color = Color.White,
                                textAlign = TextAlign.End
                            )
                        }

                        Box(
                            modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 8.dp)
                        ) {
                            // Bar color should be always light on shadowed background1
                            val barColor = if (isSystemInDarkTheme())
                                MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.inversePrimary

                            HorizontalDivider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(100))
                                    .onSizeChanged {
                                        barSize = it
                                    },
                                color = barColor.copy(0.4f),
                                thickness = 6.dp
                            )

                            HorizontalDivider(
                                modifier = Modifier
                                    .animateContentSize()
                                    .clip(RoundedCornerShape(100))
                                    .then(
                                        with(LocalDensity.current) {
                                            Modifier.size(
                                                width = barSize.width
                                                    .times(seenProgress)
                                                    .roundToInt()
                                                    .toDp(),
                                                height = barSize.height.toDp()
                                            )
                                        }
                                    ),
                                color = barColor,
                                thickness = 6.dp
                            )
                        }
                    }
                }
            }
        }
    }
}