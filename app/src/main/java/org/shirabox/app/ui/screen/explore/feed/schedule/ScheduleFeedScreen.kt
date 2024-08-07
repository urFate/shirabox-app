package org.shirabox.app.ui.screen.explore.feed.schedule

import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material3.fade
import com.google.accompanist.placeholder.material3.placeholder
import org.shirabox.app.ComposeUtils.getWeekDayTitle
import org.shirabox.app.R
import org.shirabox.app.ui.activity.resource.ResourceActivity
import org.shirabox.app.ui.component.general.ContentCardPlaceholder
import org.shirabox.app.ui.component.general.ScheduleCard
import org.shirabox.app.ui.screen.explore.ExploreViewModel
import org.shirabox.app.ui.screen.explore.TroubleMessage
import org.shirabox.core.model.ContentType
import org.shirabox.core.model.ScheduleEntry
import org.shirabox.core.util.Util
import java.util.Calendar

@Composable
fun ScheduleFeedScreen(model: ExploreViewModel = hiltViewModel()) {
    val scheduleFeedList by model.scheduleFeedList.collectAsStateWithLifecycle(initialValue = emptyList())
    val scheduleObservationStatus by model.scheduleObservationStatus.collectAsStateWithLifecycle()

    LaunchedEffect(null) { model.refresh(true) }

    val weekList = remember(scheduleFeedList) {
        scheduleFeedList.groupBy {
            val calendar = Calendar.getInstance().apply { timeInMillis = it.releaseRange.first() }
            calendar.get(Calendar.DAY_OF_WEEK)
        }
    }

    val todayScheduleList = remember(scheduleFeedList) {
        val calendar = Calendar.getInstance()

        scheduleFeedList.filter {
            val filterCalendar = Calendar.getInstance().apply { timeInMillis = it.releaseRange.first() }
            calendar.get(Calendar.DAY_OF_WEEK) == filterCalendar.get(Calendar.DAY_OF_WEEK)
        }
    }

    val isReady = remember(scheduleFeedList, scheduleObservationStatus) {
        scheduleFeedList.isNotEmpty() && scheduleObservationStatus.status == ExploreViewModel.Status.Success
    }

    TroubleMessage(observationStatus = scheduleObservationStatus)

    if(scheduleObservationStatus.status == ExploreViewModel.Status.Failure) return

    Column(
        modifier = Modifier.animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if(!isReady) {
            repeat(8) {
                ScheduleRow(title = "Lorem ipsum", isReady = false, contents = emptyList())
            }
            return
        }

        ScheduleRow(
            title = stringResource(id = R.string.schedule_today),
            contents = todayScheduleList
        )

        weekList.forEach {
            ScheduleRow(
                title = getWeekDayTitle(day = it.key),
                divider = true,
                contents = it.value
            )
        }
    }

    ScheduleDialog()
}

@Composable
fun ScheduleRow(
    title: String,
    isReady: Boolean = true,
    divider: Boolean = false,
    contents: List<ScheduleEntry>
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val cardWidth = 260
    val cardHeight = 180

    val placeholdersAmount = remember {
        Util.maxElementsInRow(itemWidth = cardWidth, configuration = configuration)
    }
    val lazyListState = rememberLazyListState()

    Column(
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .padding(16.dp, 0.dp, 0.dp, 0.dp)
                    .placeholder(
                        visible = !isReady, highlight = PlaceholderHighlight.fade()
                    ),
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight(500)
            )

            if(divider) HorizontalDivider(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp))
        }

        LazyRow(
            state = lazyListState,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            if(!isReady) items(placeholdersAmount) {
                ContentCardPlaceholder(
                    modifier = Modifier
                        .size(cardWidth.dp, cardHeight.dp)
                )
            }

            items(contents) {
                ScheduleCard(
                    modifier = Modifier.size(cardWidth.dp, cardHeight.dp),
                    scheduleEntry = it
                ) {
                    context.startActivity(
                        Intent(
                            context,
                            ResourceActivity::class.java
                        ).apply {
                            putExtra("id", it.shikimoriId)
                            putExtra("type", ContentType.ANIME)
                        }
                    )
                }
            }
        }
    }
}
