package org.shirabox.app.ui.screen.favourites

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.navigation.NavController
import org.shirabox.app.R
import org.shirabox.app.ui.activity.resource.ResourceActivity
import org.shirabox.app.ui.component.general.ContentCard
import org.shirabox.app.ui.component.general.DespondencyEmoticon
import org.shirabox.app.ui.component.top.TopBar
import org.shirabox.core.model.ContentType
import org.shirabox.core.util.Util

@Composable
fun FavouritesScreen(
    navController: NavController,
    model: FavouritesViewModel = hiltViewModel()
) {
    val favourites by model.fetchFavouriteContents().collectAsState(initial = emptyList())
    val currentType by remember {
        mutableStateOf(ContentType.ANIME)
    }

    val filteredFavourites by remember(currentType) {
        derivedStateOf {
            favourites.filter { it.type == currentType }.map { Util.mapEntityToContent(it) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopBar(navController)

        Column(
            modifier = Modifier.padding(16.dp, 0.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.favourites),
                fontSize = 22.sp,
                fontWeight = FontWeight(500)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(0.dp, 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (filteredFavourites.isEmpty()) {
                    DespondencyEmoticon(text = stringResource(id = R.string.empty_library))
                } else {
                    FavouritesGrid(contents = filteredFavourites)
                }
            }
        }
    }
}

@Composable
fun FavouritesGrid(contents: List<org.shirabox.core.model.Content>) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val cardWidth = 180
    val cardHeight = 240
    val columns = remember { configuration.screenWidthDp.floorDiv(cardWidth) }

    val gridHeight by remember(contents) {
        derivedStateOf {
            Util.calcGridHeight(
                itemsCount = contents.size,
                itemHeight = cardHeight,
                columns = columns
            ).dp
        }
    }

    LazyVerticalGrid(
        modifier = Modifier.height(gridHeight),
        columns = GridCells.Adaptive(cardWidth.minus(32).dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = false
    ) {
        items(contents) {
            ContentCard(modifier = Modifier.size(180.dp, 240.dp), item = it) {
                context.startActivity(
                    Intent(
                        context,
                        ResourceActivity::class.java
                    ).apply {
                        putExtra("id", it.shikimoriID)
                        putExtra("type", it.type)
                    }
                )
            }
        }
    }
}