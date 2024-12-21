package org.shirabox.app.ui.activity.downloads.presentation

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.shirabox.app.R
import org.shirabox.app.ui.activity.downloads.DownloadsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DownloadsTopBar(currentPage: Int, model: DownloadsViewModel = hiltViewModel()) {
    val activity = LocalContext.current as Activity
    val coroutineScope = rememberCoroutineScope()

    val dropdownExpanded = remember { mutableStateOf(false) }
    val searchMode = remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val searchQuery = model.offlineFlowFilter.collectAsStateWithLifecycle()

    LaunchedEffect(searchMode.value) {
        if (searchMode.value) focusRequester.requestFocus()
    }
    LaunchedEffect(currentPage) {
        if (currentPage < 2) searchMode.value = false
    }

    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AnimatedVisibility(
                    visible = searchMode.value,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    BasicTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        value = searchQuery.value,
                        onValueChange = {
                            coroutineScope.launch {
                                model.offlineFlowFilter.emit(it)
                            }
                        },
                        textStyle = TextStyle(
                            fontSize = 19.sp,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        decorationBox = @Composable { innerTextField ->
                            Box(
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (searchQuery.value.isEmpty()) Text(
                                    text = stringResource(R.string.search_by_name),
                                    fontSize = 19.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(0.5F)
                                )
                                innerTextField()
                            }
                        },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                        singleLine = true
                    )
                }

                AnimatedVisibility(
                    visible = !searchMode.value,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(stringResource(R.string.downloads_activity_title))
                }
            }
        },
        navigationIcon = {
            IconButton(
                onClick = activity::finish
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = "back"
                )
            }
        },
        actions = {
            AnimatedVisibility(visible = currentPage == 2, enter = fadeIn(), exit = fadeOut()) {
                val vector = remember(searchMode.value) {
                    if (searchMode.value) Icons.Rounded.Close else Icons.Rounded.Search
                }

                IconButton(onClick = {
                    searchMode.value = searchMode.value.not()
                    coroutineScope.launch {
                        model.offlineFlowFilter.emit("")
                    }
                }) {
                    Icon(
                        imageVector = vector,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        contentDescription = "Search"
                    )
                }
            }

            if (currentPage < 2) {
                IconButton(onClick = { dropdownExpanded.value = true }) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        contentDescription = "More"
                    )
                }
            }

            DropdownMenu(
                expanded = dropdownExpanded.value,
                onDismissRequest = { dropdownExpanded.value = false },
            ) {
                DropdownMenuItem(text = { Text(text = stringResource(R.string.downloads_stop_all)) },
                    onClick = {
                        when (currentPage) {
                            0 -> model.cancelEnqueuedTasks()
                            1 -> model.cancelAllPausedTasks()
                        }
                        dropdownExpanded.value = false
                    })
            }
        })

}