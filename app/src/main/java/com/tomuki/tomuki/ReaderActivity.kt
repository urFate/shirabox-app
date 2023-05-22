package com.tomuki.tomuki

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.tomuki.tomuki.ui.theme.TomukiTheme
import kotlin.math.abs
import kotlin.math.withSign


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
    val hideSystemBars = remember {
        mutableStateOf(false)
    }
    val scrollEnabled = remember { mutableStateOf(true) }
    val systemUiController = rememberSystemUiController()

    systemUiController.isSystemBarsVisible = hideSystemBars.value

    Scaffold(
        topBar = { ReaderTopBar("Том 1 Глава 1", hideSystemBars.value) },
        bottomBar = { ReaderBottomBar(hideSystemBars.value) },
    ) {
        Box(
            modifier = Modifier
                .padding(0.dp)
                .fillMaxSize()
        ) {
            val pages = listOf(
                ImageBitmap.imageResource(id = R.drawable.blank),
                ImageBitmap.imageResource(id = R.drawable.blank),
                ImageBitmap.imageResource(id = R.drawable.blank)
            )

            ReaderPager(
                mode = ReaderMode.HORIZONTAL,
                pagerState = pagerState,
                hideSystemBars = hideSystemBars,
                scrollEnabled = scrollEnabled,
                pages = pages
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                text = "${pagerState.currentPage + 1}/${pages.size}",
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderTopBar(title: String, isVisible: Boolean){
    AnimatedVisibility(
        visible = isVisible,
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
}

@Composable
fun ReaderBottomBar(isVisible: Boolean) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
    ) {
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
}

@SuppressLint("InflateParams")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReaderPager(
    modifier: Modifier = Modifier,
    mode: ReaderMode,
    pagerState: PagerState,
    hideSystemBars: MutableState<Boolean>,
    pages: List<ImageBitmap>,
    scrollEnabled: MutableState<Boolean>
){
    when(mode){
        ReaderMode.HORIZONTAL -> {
            HorizontalPager(
                modifier = modifier,
                pageCount = pages.size,
                state = pagerState,
                flingBehavior = PagerDefaults.flingBehavior(state = pagerState),
                userScrollEnabled = scrollEnabled.value
            ) {
                ZoomablePagerImage(bitmap = pages[it],
                    hideSystemBars = hideSystemBars,
                    scrollEnabled = scrollEnabled,
                    modifier = Modifier.fillMaxSize())
            }
        }
        ReaderMode.VERTICAL -> {
            VerticalPager(
                modifier = modifier,
                pageCount = pages.size,
                state = pagerState,
                flingBehavior = PagerDefaults.flingBehavior(state = pagerState),
                userScrollEnabled = scrollEnabled.value
            ) {
                ZoomablePagerImage(bitmap = pages[it],
                    hideSystemBars = hideSystemBars,
                    scrollEnabled = scrollEnabled,
                    modifier = Modifier.fillMaxSize())
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

// TODO: думаю хорошо бы это вынесты в отдельный файл
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ZoomablePagerImage(
    modifier: Modifier = Modifier,
    bitmap: ImageBitmap,
    scrollEnabled: MutableState<Boolean>,
    minScale: Float = 1f,
    maxScale: Float = 5f,
    contentScale: ContentScale = ContentScale.Fit,
    isRotation: Boolean = false,
    hideSystemBars: MutableState<Boolean>
) {
    var targetScale by remember { mutableStateOf(1f) }
    val scale = animateFloatAsState(targetValue = maxOf(minScale, minOf(maxScale, targetScale)))
    var rotationState by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(1f) }
    var offsetY by remember { mutableStateOf(1f) }
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
    Box(
        modifier = Modifier
            .clip(RectangleShape)
            .background(Color.Transparent)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { },
                onDoubleClick = {
                    if (targetScale >= 1.5f) {
                        targetScale = 1f
                        offsetX = 1f
                        offsetY = 1f
                        scrollEnabled.value = true
                        hideSystemBars.value = true
                    } else targetScale = 3f
                },
            )
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    do {
                        val event = awaitPointerEvent()
                        val zoom = event.calculateZoom()
                        targetScale *= zoom
                        val offset = event.calculatePan()
                        if (targetScale <= 1) {
                            offsetX = 1f
                            offsetY = 1f
                            targetScale = 1f
                            scrollEnabled.value = true
                            hideSystemBars.value = true
                        } else {
                            offsetX += offset.x
                            offsetY += offset.y
                            if (zoom > 1) {
                                scrollEnabled.value = false
                                hideSystemBars.value = false
                                rotationState += event.calculateRotation()
                            }
                            val imageWidth = screenWidthPx * scale.value
                            val borderReached = imageWidth - screenWidthPx - 2 * abs(offsetX)
                            scrollEnabled.value = borderReached <= 0
                            hideSystemBars.value = scrollEnabled.value
                            if (borderReached < 0) {
                                offsetX = ((imageWidth - screenWidthPx) / 2f).withSign(offsetX)
                                if (offset.x != 0f) offsetY -= offset.y
                            }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }

    ) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            contentScale = contentScale,
            modifier = modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    this.scaleX = scale.value
                    this.scaleY = scale.value
                    if (isRotation) {
                        rotationZ = rotationState
                    }
                    this.translationX = offsetX
                    this.translationY = offsetY
                }
        )
    }
}