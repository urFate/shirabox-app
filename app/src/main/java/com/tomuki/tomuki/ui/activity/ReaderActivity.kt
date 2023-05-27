package com.tomuki.tomuki.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.tomuki.tomuki.R
import com.tomuki.tomuki.ui.theme.TomukiTheme
import de.mr_pine.zoomables.ZoomableImage
import de.mr_pine.zoomables.rememberZoomableState
import kotlinx.coroutines.launch

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

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReaderScaffold(){
    val pagerState = rememberPagerState()
    val hideSystemBars = remember { mutableStateOf(false) }
    val scrollEnabled = remember { mutableStateOf(true) }
    val systemUiController = rememberSystemUiController()

    val pages = listOf(
        ImageBitmap.imageResource(id = R.drawable.blank),
        ImageBitmap.imageResource(id = R.drawable.blank),
        ImageBitmap.imageResource(id = R.drawable.blank)
    )

    systemUiController.isSystemBarsVisible = hideSystemBars.value

    Scaffold(
        topBar = { ReaderTopBar("Том 1 Глава 1", hideSystemBars.value) },
        bottomBar = { ReaderBottomBar(hideSystemBars.value, pagerState, pages.size) }
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            ReaderPager(
                pagerState = pagerState,
                hideSystemBars = hideSystemBars,
                scrollEnabled = scrollEnabled,
                pages = pages
            )

//            Text(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .align(Alignment.BottomCenter)
//                    .padding(16.dp),
//                text = "${pagerState.currentPage + 1}/${pages.size}",
//                fontWeight = FontWeight.Medium,
//                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                textAlign = TextAlign.Center,
//            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderTopBar(title: String, isVisible: Boolean){
    val activity = (LocalContext.current as? Activity)
    AnimatedVisibility(
        visible = !isVisible,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it }),
    ) {
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
                    onClick = { activity?.finish() }
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReaderBottomBar(
    isVisible: Boolean,
    pagerState: PagerState,
    pagesSize: Int
) {
    LocalConfiguration.current
    val context = (LocalContext.current as? Activity)
    val scope = rememberCoroutineScope()

    AnimatedVisibility(
        visible = !isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
    ) {
        Column {
            Slider(
                value = pagerState.currentPage.toFloat(),
                onValueChange = { newValue ->
                    scope.launch{
                        pagerState.animateScrollToPage(newValue.toInt())
                    }
                },
                valueRange = 0f..(pagesSize - 1).toFloat(),
                steps = pagesSize - 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

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
                IconButton(onClick = {
                    val newOrientation = when (context?.resources?.configuration?.orientation) {
                        Configuration.ORIENTATION_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        Configuration.ORIENTATION_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    }
                    context?.requestedOrientation = newOrientation
                }) {
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
    }
}

@SuppressLint("InflateParams")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReaderPager(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    hideSystemBars: MutableState<Boolean>,
    pages: List<ImageBitmap>,
    scrollEnabled: MutableState<Boolean>
){
    HorizontalPager(
        modifier = modifier,
        pageCount = pages.size,
        state = pagerState,
        flingBehavior = PagerDefaults.flingBehavior(state = pagerState),
        userScrollEnabled = scrollEnabled.value
    ) {
        ZoomableImage(
            modifier = Modifier
                .fillMaxSize(),
            coroutineScope = rememberCoroutineScope(),
            zoomableState = rememberZoomableState(),
            painter = painterResource(id = R.drawable.blank),
            onTap = { hideSystemBars.value = !hideSystemBars.value }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ReaderPreview() {
    TomukiTheme {
        ReaderScaffold()
    }
}
