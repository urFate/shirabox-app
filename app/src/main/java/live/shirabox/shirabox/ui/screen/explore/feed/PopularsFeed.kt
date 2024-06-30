package live.shirabox.shirabox.ui.screen.explore.feed

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
internal fun PopularsFeed(isReady: Boolean, contents: List<Content>) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val cardWidth = 180
    val cardHeight = 240
    val columns = remember { configuration.screenWidthDp.floorDiv(cardWidth) }

    val gridHeight by remember(contents) {
        derivedStateOf {
            Util.calcGridHeight(
                itemsCount = contents.size.plus(2),
                itemHeight = cardHeight,
                columns = columns
            ).dp
        }
    }

    Column(
        modifier = Modifier.padding(16.dp, 0.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            modifier = Modifier
                .placeholder(
                    visible = !isReady,
                    highlight = PlaceholderHighlight.fade()
                ),
            text = stringResource(R.string.popular),
            fontSize = 22.sp,
            fontWeight = FontWeight(500)
        )

        LazyVerticalGrid(
            modifier = Modifier.height(gridHeight),
            columns = GridCells.Adaptive(cardWidth.minus(32).dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            userScrollEnabled = false
        ) {
            items(contents) {
                BaseCard(
                    modifier = Modifier
                        .size(cardWidth.dp, cardHeight.dp),
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

            items(2) {
                ContentCardPlaceholder(modifier = Modifier.size(cardWidth.dp, cardHeight.dp))
            }
        }
    }
}