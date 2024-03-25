package live.shirabox.shirabox.ui.activity.search

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import live.shirabox.core.model.ContentType
import live.shirabox.data.catalog.shikimori.ShikimoriRepository
import live.shirabox.shirabox.R
import live.shirabox.shirabox.ui.activity.resource.ResourceActivity
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen() {
    val context = LocalContext.current
    val activity = context as? Activity

    var text by remember { mutableStateOf("") }
    val searchHistory = remember { mutableStateListOf("") }
    val focusRequester = remember { FocusRequester() }

    val searchStateFlow =
        ShikimoriRepository.search(text, ContentType.ANIME).collectAsState(initial = null)

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

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

            if (searchStateFlow.value == null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            searchStateFlow.value?.let { contents ->
                items(contents) {
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