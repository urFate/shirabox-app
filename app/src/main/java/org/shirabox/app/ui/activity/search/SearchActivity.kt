    package org.shirabox.app.ui.activity.search

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import org.shirabox.app.R
import org.shirabox.app.ValuesHelper
import org.shirabox.app.ui.activity.resource.ResourceActivity
import org.shirabox.app.ui.component.general.DespondencyEmoticon
import org.shirabox.app.ui.component.general.ListItem
import org.shirabox.app.ui.theme.ShiraBoxTheme
import org.shirabox.core.model.Content
import org.shirabox.core.model.ContentType
import org.shirabox.data.catalog.shikimori.ShikimoriRepository

    class SearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShiraBoxTheme(
                transparentStatusBar = true
            ) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) { SearchScreen() }
            }

            enableEdgeToEdge()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) registerOnBackInvokedCallback()
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            this@SearchActivity.finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun registerOnBackInvokedCallback() {
        onBackInvokedDispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_OVERLAY) {
            this@SearchActivity.finish()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen() {
    val context = LocalContext.current
    val activity = context as? Activity

    var text by remember { mutableStateOf("") }
    var showProgressIndicator by remember { mutableStateOf(true) }
    val searchHistory = remember { mutableStateListOf("") }
    val focusRequester = remember { FocusRequester() }
    val queryText = remember { mutableStateOf("") }
    val resultsList = remember<SnapshotStateList<Content>>(::mutableStateListOf)

    val searchStateFlow =
        ShikimoriRepository.search(queryText.value, ContentType.ANIME).catch {
            it.printStackTrace()
            emitAll(emptyFlow())
        }.collectAsState(initial = null)


    val noResultsState = remember(queryText.value, searchStateFlow.value) {
        derivedStateOf {
            queryText.value.isNotEmpty() && searchStateFlow.value.isNullOrEmpty()
        }
    }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    LaunchedEffect(searchStateFlow.value) {
        showProgressIndicator = false
        searchStateFlow.value?.let(resultsList::addAll)
    }

    LaunchedEffect(text) {
        showProgressIndicator = true
        resultsList.clear()
        delay(450L)
        queryText.value = text
    }

    Column {
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = text,
                    onQueryChange = { text = it },
                    onSearch = { searchHistory.add(text) },
                    expanded = true,
                    onExpandedChange = {  },
                    enabled = true,
                    placeholder = {
                        Text(text = stringResource(id = R.string.search_by_name))
                    },
                    leadingIcon = {
                        Icon(painter = painterResource(R.drawable.search), contentDescription = "Search icon")
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            if (text.isNotEmpty()) {
                                searchHistory.add(text)
                                text = ""
                            } else activity?.finish()
                        }) {
                            Icon(painter = painterResource(R.drawable.close), contentDescription = "Close icon")
                        }
                    },
                    interactionSource = null,
                )
            },
            expanded = true,
            onExpandedChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            shape = SearchBarDefaults.inputFieldShape,
            tonalElevation = SearchBarDefaults.TonalElevation,
            shadowElevation = SearchBarDefaults.ShadowElevation,
            windowInsets = SearchBarDefaults.windowInsets,
            content = {
                LazyColumn(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (text.isEmpty()) {
                        items(searchHistory) {
                            if (it.isNotEmpty()) {
                                androidx.compose.material3.ListItem(
                                    modifier = Modifier.clickable {
                                        text = it
                                    },
                                    headlineContent = {
                                        Text(it)
                                    },
                                    leadingContent = {
                                        Icon(
                                            painter = painterResource(R.drawable.undo),
                                            contentDescription = null
                                        )
                                    },
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                                )
                            }
                        }
                        return@LazyColumn
                    }

                    item {
                        AnimatedVisibility(
                            visible = showProgressIndicator && !noResultsState.value
                        ) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    item {
                        AnimatedVisibility(
                            visible = noResultsState.value,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                DespondencyEmoticon(text = stringResource(id = R.string.nothing_found))
                            }
                        }
                    }

                    items(resultsList) {
                        AnimatedVisibility(
                            visible = !showProgressIndicator,
                            enter = fadeIn(
                                animationSpec = tween(300, easing = LinearEasing)
                            )
                        ) {
                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = it.name.ifBlank { it.enName },
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                supportingString = "${it.releaseYear}, ${
                                    ValuesHelper.decodeKind(
                                        it.kind,
                                        context
                                    )
                                }",
                                coverImage = it.image,
                            ) {
                                context.startActivity(Intent(
                                    context, ResourceActivity::class.java
                                ).apply {
                                    putExtra("id", it.shikimoriId)
                                    putExtra("type", it.type)
                                })
                            }
                        }
                    }
                }
            },
        )
    }
}