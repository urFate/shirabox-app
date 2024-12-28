package org.shirabox.app.ui.component.general

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import org.shirabox.app.ui.screen.explore.ExploreViewModel
import org.shirabox.core.model.ScheduleEntry
import org.shirabox.core.util.getDuration
import org.shirabox.app.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScheduleCard(
    modifier: Modifier = Modifier,
    scheduleEntry: ScheduleEntry,
    model: ExploreViewModel = hiltViewModel(),
    onClick: () -> Unit
) {
    val time = remember(scheduleEntry) {
        val firstTimeLabel = scheduleEntry.releaseRange.first().getDuration()
        val secondTimeLabel = scheduleEntry.releaseRange.getOrNull(1)?.getDuration()

        firstTimeLabel.plus(secondTimeLabel?.let { " - $it" })
    }

    val cachedContentState = model.cachedContentFlow(scheduleEntry.id).collectAsStateWithLifecycle(null)

    val isFavourite = remember(cachedContentState.value) {
        cachedContentState.value?.isFavourite == true
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
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth(),
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
                            text = stringResource(R.string.schedule_episode, scheduleEntry.nextEpisodeNumber),
                            color = if (scheduleEntry.released) Color(0xFF7BC251) else Color(0xFF323232)
                        )
                    }

                    AnimatedVisibility(visible = isFavourite, enter = fadeIn(), exit = fadeOut()) {
                        Icon(
                            modifier = Modifier.size(22.dp),
                            imageVector = Icons.Rounded.Star,
                            contentDescription = "favourite",
                            tint = Color(0xFFFFD700)
                        )
                    }
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
