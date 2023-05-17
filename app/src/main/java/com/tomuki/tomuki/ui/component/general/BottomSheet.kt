package com.tomuki.tomuki.ui.component.general

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch

class BottomSheet {
    private var openBottomSheet by mutableStateOf(false)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BottomSheetComponent(content: @Composable ColumnScope.() -> Unit){
        val coroutineScope = rememberCoroutineScope()
        val skipPartiallyExpanded by remember { mutableStateOf(false) }
        val state = rememberModalBottomSheetState(
            skipPartiallyExpanded = skipPartiallyExpanded
        )

        if(openBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    coroutineScope.launch { state.hide() }
                    visibility(false)
                },
                sheetState = state,
                content = content
            )
        }
    }

    fun visibility(isVisible: Boolean) {
        openBottomSheet = isVisible
    }
}