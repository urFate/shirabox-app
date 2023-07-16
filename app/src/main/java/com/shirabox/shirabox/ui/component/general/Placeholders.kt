package com.shirabox.shirabox.ui.component.general

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material3.fade
import com.google.accompanist.placeholder.material3.placeholder

@Composable
fun RowPlaceholder() {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    Row(
        modifier = Modifier.padding(16.dp, 0.dp, 0.dp, 0.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val itemsCount = (screenWidth / 160).inc()

        for (i in 1..itemsCount) {
            Box(
                modifier = Modifier
                    .size(160.dp, 220.dp)
                    .placeholder(
                        visible = true,
                        shape = RoundedCornerShape(10),
                        highlight = PlaceholderHighlight.fade()
                    ),
            )
        }
    }
}

@Composable
fun GridPlaceholder() {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp

    val columnsCount = ((screenHeight / 2) / 240)
    val itemsInRowCount = screenWidth / 180

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(columnsCount) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                repeat(itemsInRowCount) {
                    Box(
                        modifier = Modifier
                            .size(180.dp, 240.dp)
                            .placeholder(
                                visible = true,
                                shape = RoundedCornerShape(10),
                                highlight = PlaceholderHighlight.fade()
                            )
                    )
                }
            }
        }
    }
}