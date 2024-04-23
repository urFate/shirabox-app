    package live.shirabox.shirabox.ui.activity.search

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import live.shirabox.core.model.Content
import live.shirabox.core.model.ContentType
import live.shirabox.data.catalog.shikimori.ShikimoriRepository
import live.shirabox.shirabox.R
import live.shirabox.shirabox.ui.activity.resource.ResourceActivity
import live.shirabox.shirabox.ui.component.general.DespondencyEmoticon
import live.shirabox.shirabox.ui.component.general.ListItem
import live.shirabox.shirabox.ui.theme.ShiraBoxTheme

    class SearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShiraBoxTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) { SearchScreen() }
            }

            enableEdgeToEdge()
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

    SearchBar(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        query = text,
        onQueryChange = { text = it },
        onSearch = { searchHistory.add(text) },
        active = true,
        onActiveChange = {},
        placeholder = {
            Text(text = stringResource(id = R.string.search_by_name))
        },
        leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Search icon")
        },
        trailingIcon = {
            IconButton(onClick = {
                if (text.isNotEmpty()) {
                    searchHistory.add(text)
                    text = ""
                } else activity?.finish()
            }) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close icon")
            }
        }) {

        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (text.isEmpty()) {
                items(searchHistory) {
                    if (it.isNotEmpty()) {
                        androidx.compose.material3.ListItem(modifier = Modifier.clickable {
                                text = it
                            }, headlineContent = {
                            Text(it)
                        }, leadingContent = {
                            Icon(imageVector = Icons.Default.History, contentDescription = null)
                        })
                    }
                }
                return@LazyColumn
            }

            item {
                if(showProgressIndicator && !noResultsState.value) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            strokeCap = StrokeCap.Round
                        )
                    }
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
                                text = it.name, fontWeight = FontWeight.Bold
                            )
                        }, supportingString = "${it.releaseYear}, ${it.kind}", coverImage = it.image
                    ) {
                        context.startActivity(Intent(
                            context, ResourceActivity::class.java
                        ).apply {
                            putExtra("id", it.shikimoriID)
                            putExtra("type", it.type)
                        })
                    }
                }
            }
        }
    }
}