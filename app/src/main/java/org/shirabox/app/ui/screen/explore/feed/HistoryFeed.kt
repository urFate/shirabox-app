package org.shirabox.app.ui.screen.explore.feed

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material3.fade
import com.google.accompanist.placeholder.material3.placeholder
import org.shirabox.app.R
import org.shirabox.app.ui.activity.player.PlayerActivity
import org.shirabox.app.ui.component.general.ContentCardPlaceholder
import org.shirabox.app.ui.component.general.PreviewCard
import org.shirabox.core.entity.EpisodeEntity
import org.shirabox.core.entity.relation.CombinedContent
import org.shirabox.core.util.IntentExtras
import org.shirabox.core.util.Util

@Composable
internal fun HistoryFeed(
    isReady: Boolean,
    contents: Map<CombinedContent, EpisodeEntity>
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val cardWidth = 260
    val cardHeight = 180

    val placeholdersAmount = remember {
        Util.maxElementsInRow(itemWidth = cardWidth, configuration = configuration)
    }

    AnimatedVisibility(
        visible = contents.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                modifier = Modifier
                    .padding(16.dp, 0.dp)
                    .placeholder(
                        visible = !isReady, highlight = PlaceholderHighlight.fade()
                    ),
                text = stringResource(id = R.string.continue_watching_feed),
                fontSize = 22.sp,
                fontWeight = FontWeight(500)
            )

            LazyRow(
                state = rememberLazyListState(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                if(!isReady) items(placeholdersAmount) {
                    ContentCardPlaceholder(Modifier.size(cardWidth.dp, cardHeight.dp))
                }

                contents.forEach { (combinedContent, entity) ->
                    item {
                        PreviewCard(
                            modifier = Modifier.size(cardWidth.dp, cardHeight.dp),
                            title = combinedContent.content.name,
                            team = entity.actingTeamName,
                            episode = entity.episode,
                            kind = combinedContent.content.kind,
                            imagePath = combinedContent.content.image,
                            watchingTime = entity.watchingTime,
                            streamLength = entity.videoLength ?: 1200000
                        ) {
                            onClick(combinedContent, context, entity)
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(32.dp, 8.dp)
            )
        }
    }
}

private fun onClick(
    combinedContent: CombinedContent,
    context: Context,
    entity: EpisodeEntity
) {
    context.startActivity(
        Intent(context, PlayerActivity::class.java).apply {
            putExtras(
                IntentExtras.playerIntentExtras(
                    content = Util.mapEntityToContent(combinedContent.content),
                    episodeEntity = entity,
                    team = entity.actingTeamName,
                )
            )
        }
    )
}
