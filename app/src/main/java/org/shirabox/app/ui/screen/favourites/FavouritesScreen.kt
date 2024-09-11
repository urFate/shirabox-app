package org.shirabox.app.ui.screen.favourites

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.shirabox.app.R
import org.shirabox.app.ValuesHelper
import org.shirabox.app.ui.activity.resource.ResourceActivity
import org.shirabox.app.ui.component.general.BaseCard
import org.shirabox.app.ui.component.general.DespondencyEmoticon
import org.shirabox.app.ui.component.top.TopBar
import org.shirabox.core.model.Content
import org.shirabox.core.model.ContentKind
import org.shirabox.core.util.Util

@Composable
fun FavouritesScreen(
    navController: NavController,
    model: FavouritesViewModel = hiltViewModel()
) {
    val favourites by model.fetchFavouriteContents().collectAsState(initial = emptyList())
    val bottomSheetVisibilityState = remember { mutableStateOf(false) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.favourites),
                    fontSize = 22.sp,
                    fontWeight = FontWeight(500)
                )

                TextButton(onClick = { bottomSheetVisibilityState.value = true }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FilterList,
                            contentDescription = "Filter",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = stringResource(id = R.string.sorting),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(0.dp, 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (favourites.isEmpty()) {
                    DespondencyEmoticon(text = stringResource(id = R.string.empty_library_filter))
                } else {
                    FavouritesGrid(contents = favourites)
                }
            }
        }
    }

    if (bottomSheetVisibilityState.value) SortingSheetScreen(visibilityState = bottomSheetVisibilityState)
}

@Composable
fun FavouritesGrid(contents: List<Content>) {
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
            BaseCard(
                modifier = Modifier.size(180.dp, 240.dp),
                title = it.name.ifBlank { it.enName },
                image = it.image,
                type = it.type
            ) {
                context.startActivity(
                    Intent(
                        context,
                        ResourceActivity::class.java
                    ).apply {
                        putExtra("id", it.shikimoriId)
                        putExtra("type", it.type)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SortingSheetScreen(
    visibilityState: MutableState<Boolean>,
    model: FavouritesViewModel = hiltViewModel()
) {
    val skipPartiallyExpanded by remember { mutableStateOf(false) }
    val state = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val selectedSortType by remember { derivedStateOf { model.selectedSortType.value } }
    val selectedKind by remember { derivedStateOf { model.selectedKind.value } }

    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = {
            coroutineScope.launch {
                state.hide()
                visibilityState.value = false
            }
        },
        contentWindowInsets = { BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Bottom) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp, 0.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.sorting),
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.W400,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = stringResource(id = R.string.sort_by),
                color = MaterialTheme.colorScheme.onSurface
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SortType.entries.forEach { sortType ->
                    val selected = remember(selectedSortType) { selectedSortType == sortType }

                    MyFilterChip(
                        selected = selected,
                        label = { Text(text = ValuesHelper.decodeSortingType(sortType, context)) }
                    ) {
                        model.selectedSortType.value = sortType
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            Text(
                text = stringResource(id = R.string.kind),
                color = MaterialTheme.colorScheme.onSurface
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ContentKind.entries.forEach { kind ->
                    val selected = remember(selectedKind) { selectedKind == kind }

                    MyFilterChip(
                        selected = selected,
                        label = { Text(text = ValuesHelper.decodeKind(kind, context)) }
                    ) {
                        model.selectedKind.value = kind
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .height(48.dp),
                    onClick = {
                        coroutineScope.launch {
                            state.hide()
                            visibilityState.value = false
                        }

                        model.selectedSortType.value = SortType.DEFAULT
                        model.selectedKind.value = null
                    },
                ) {
                    Text(text = stringResource(id = R.string.reset))
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .height(48.dp),
                    onClick = {
                        coroutineScope.launch {
                            state.hide()
                            visibilityState.value = false
                        }
                    }
                ) {
                    Text(text = stringResource(id = R.string.apply))
                }
            }
        }
    }
}

@Composable
private fun MyFilterChip(
    selected: Boolean,
    label: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val chipColors = FilterChipDefaults.filterChipColors().copy(
        labelColor = MaterialTheme.colorScheme.primary,
        leadingIconColor = MaterialTheme.colorScheme.primary
    )
    val borderBrush = Brush.linearGradient(
        if (!selected) listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary
        ) else listOf(Color.Transparent, Color.Transparent)
    )
    val border = FilterChipDefaults.filterChipBorder(true, selected).copy(
        brush = borderBrush
    )

    FilterChip(
        onClick = onClick,
        selected = selected,
        label = label,
        leadingIcon = {
            if (selected) Icon(
                modifier = Modifier.size(16.dp),
                imageVector = Icons.Rounded.Done,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        colors = chipColors,
        border = border
    )
}