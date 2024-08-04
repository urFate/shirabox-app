package org.shirabox.app.ui.screen.explore.feed.schedule

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.shirabox.app.ui.component.general.DisposableScheduleDialog
import org.shirabox.core.datastore.AppDataStore
import org.shirabox.core.datastore.DataStoreScheme

@Composable
fun ScheduleDialog() {
    val context = LocalContext.current
    val coroutineContext = rememberCoroutineScope()
    val isOpen = remember { mutableStateOf(false) }

    val isDialogConfirmed =
        AppDataStore.read(context, DataStoreScheme.FIELD_SCHEDULE_DIALOG_CONFIRMATION)
            .collectAsStateWithLifecycle(initialValue = true)

    LaunchedEffect(isDialogConfirmed.value) {
        isOpen.value = isDialogConfirmed.value?.not() ?: true
    }

    if(isOpen.value) {
        DisposableScheduleDialog(isOpen = isOpen) {
            coroutineContext.launch {
                AppDataStore.write(context, DataStoreScheme.FIELD_SCHEDULE_DIALOG_CONFIRMATION, true)
            }
        }
    }
}