package live.shirabox.shirabox.ui.screen.explore.feed

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import live.shirabox.core.model.Content
import live.shirabox.core.util.Util
import live.shirabox.shirabox.R
import live.shirabox.shirabox.ui.activity.resource.ResourceActivity
import live.shirabox.shirabox.ui.component.general.BaseCard
import live.shirabox.shirabox.ui.component.general.ContentCardPlaceholder

@Composable
internal fun TrendingFeed(
    isReady: Boolean,
    contents: List<Content>
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val cardWidth = 160

    val placeholdersAmount = remember {
        Util.maxElementsInRow(itemWidth = cardWidth, configuration = configuration)
    }
    val lazyListState = rememberLazyListState()

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            modifier = Modifier
                .padding(16.dp, 0.dp)
                .placeholder(
                    visible = !isReady, highlight = PlaceholderHighlight.fade()
                ),
            text = stringResource(id = R.string.actual),
            fontSize = 22.sp,
            fontWeight = FontWeight(500)
        )

        LazyRow(
            state = lazyListState,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            if(!isReady) items(placeholdersAmount) {
                ContentCardPlaceholder(
                    modifier = Modifier
                        .size(cardWidth.dp, 220.dp)
                )
            }

            items(contents) {
                BaseCard(
                    modifier = Modifier.size(cardWidth.dp, 220.dp),
                    title = it.name, image = it.image, type = it.type
                ) {
                    context.startActivity(
                        Intent(
                            context,
                            ResourceActivity::class.java
                        ).apply {
                            putExtra("id", it.shikimoriID)
                            putExtra("type", it.type.toString())
                        }
                    )
                }
            }
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(32.dp, 8.dp)
    )
}