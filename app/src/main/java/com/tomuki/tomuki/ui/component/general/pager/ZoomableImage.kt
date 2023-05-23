package com.tomuki.tomuki.ui.component.general.pager

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.withSign

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
                onClick = {  },
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
                    hideSystemBars.value = !hideSystemBars.value
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
