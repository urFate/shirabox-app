package com.tomuki.tomuki.ui.component.general

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp

@Composable
fun CellGrid(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical,
    horizontalArrangement: Arrangement.Horizontal,
    cellWidth: Dp,
    contents: List<@Composable () -> Unit>
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    val itemsInRowCount = (screenWidth / cellWidth.value).toInt()

    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement
    ) {
        contents.chunked(itemsInRowCount).forEach {
            Row(
                horizontalArrangement = horizontalArrangement
            ) {
                it.forEach { it() }
            }
        }
    }
}