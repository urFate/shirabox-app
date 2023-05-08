package com.tomuki.tomuki.ui.screen.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tomuki.tomuki.ui.component.top.MediaTypesBar
import com.tomuki.tomuki.ui.component.top.TopBar

@ExperimentalMaterial3Api
@Preview
@Composable
fun ExploreScreen() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopBar("Поиск по названию")
        MediaTypesBar()
    }
}