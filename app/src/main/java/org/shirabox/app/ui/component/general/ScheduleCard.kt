package org.shirabox.app.ui.component.general

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.shirabox.core.model.ScheduleEntry
import org.shirabox.core.util.getDuration

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScheduleCard(
    modifier: Modifier = Modifier,
    scheduleEntry: ScheduleEntry,
    onClick: () -> Unit
) {
    val time = remember(scheduleEntry) {
        val firstTimeLabel = scheduleEntry.releaseRange.first().getDuration()
        val secondTimeLabel = scheduleEntry.releaseRange.getOrNull(1)?.let {
            it.getDuration()
        }

        "$firstTimeLabel".plus(secondTimeLabel?.let { " - $it" })
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10),
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier.clickable { onClick() }
        ) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxSize(),
                model = scheduleEntry.image,
                contentDescription = scheduleEntry.image,
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
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
                ) {
                    if(!scheduleEntry.released) {
                        ScheduleCardBadge(
                            text = time,
                            color = Color(0xFF6750A4)
                        )
                    }

                    ScheduleCardBadge(
                        text = "Серия ${scheduleEntry.nextEpisodeNumber}",
                        color = if (scheduleEntry.released) Color(0xFF7BC251) else Color(0xFF323232)
                    )
                }

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
                            text = scheduleEntry.russianName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Start
                        )
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = scheduleEntry.name,
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

@Composable
private fun ScheduleCardBadge(text: String, textColor: Color = Color.White, color: Color) {
    val shape = RoundedCornerShape(60)

    Card(
        modifier = Modifier.wrapContentSize(),
        shape = shape,
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors().copy(containerColor = color)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.wrapContentSize(),
        ) {
            Text(
                modifier = Modifier.padding(16.dp, 0.dp),
                text = text,
                color = textColor,
                fontSize = 12.sp,
                maxLines = 1,
            )
        }
    }
}
