package com.tomuki.tomuki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.ControlCamera
import androidx.compose.material.icons.outlined.ScreenRotation
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tomuki.tomuki.ui.theme.TomukiTheme
import soup.compose.photo.ExperimentalPhotoApi
import soup.compose.photo.PhotoBox
import soup.compose.photo.rememberPhotoState

class ReaderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TomukiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ReaderScaffold()
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReaderScaffold(){
    val pagerState = rememberPagerState()

    Scaffold(
        topBar = { ReaderTopBar("Том 1 Глава 1") },
        bottomBar = { ReaderBottomBar() }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            ReaderPager(
                mode = ReaderMode.HORIZONTAL,
                pagerState = pagerState,
                pages = listOf(
                    {
                        Image(
                            modifier = Modifier.fillMaxSize(),
                            painter = painterResource(id = R.drawable.blank),
                            contentDescription = null
                        )
                    },
                    {
                        Image(
                            modifier = Modifier.fillMaxSize(),
                            painter = painterResource(id = R.drawable.blank),
                            contentDescription = null
                        )
                    }
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderTopBar(title: String){
    TopAppBar(
        title = { Text(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            text = title,
            overflow = TextOverflow.Ellipsis
        ) },
        navigationIcon = {
            IconButton(
                onClick = { /*TODO*/ }
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Arrow Back",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_captive_portal),
                    contentDescription = "Open In Web",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors()
    )
}

@Composable
fun ReaderBottomBar(){
    BottomAppBar {

        // Reading mode
        IconButton(onClick = { /* TODO */ }) {
            Icon(
                imageVector = Icons.Outlined.ControlCamera,
                contentDescription = "Reading Mode",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Screen rotation
        IconButton(onClick = { /* TODO */ }) {
            Icon(
                imageVector = Icons.Outlined.ScreenRotation,
                contentDescription = "Screen Rotation",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Add to bookmarks
        IconButton(onClick = { /* TODO */ }) {
            Icon(
                imageVector = Icons.Outlined.BookmarkAdd,
                contentDescription = "Bookmark Add",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalPhotoApi::class)
@Composable
fun ReaderPager(mode: ReaderMode, pagerState: PagerState, pages: List<@Composable (Int) -> Unit>){
    when(mode){
        ReaderMode.HORIZONTAL -> {
            HorizontalPager(
                pageCount = pages.size,
                state = pagerState,
                flingBehavior = PagerDefaults.flingBehavior(state = pagerState)
            ) {
                val photoState = rememberPhotoState()
                photoState.setPhotoIntrinsicSize(Size.Unspecified)

                PhotoBox(
                    state = photoState
                ) { pages[it].invoke(it) }
            }
        }
        ReaderMode.VERTICAL -> {
            VerticalPager(
                pageCount = pages.size,
                state = pagerState,
                flingBehavior = PagerDefaults.flingBehavior(state = pagerState)
            ) {
                val photoState = rememberPhotoState()
                photoState.setPhotoIntrinsicSize(Size.Unspecified)

                PhotoBox(
                    state = photoState
                ) { pages[it].invoke(it) }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReaderPreview() {
    TomukiTheme {
        ReaderScaffold()
    }
}

enum class ReaderMode {
    HORIZONTAL, VERTICAL
}