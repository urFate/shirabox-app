package org.shirabox.app.ui.activity.player.presentation.controls

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.shirabox.app.R

@Composable
internal fun PlayerSkipButton(
    autoSkip: Boolean,
    isPlaying: Boolean,
    onTimeout: () -> Unit,
    onClick: () -> Unit
) {
    var percentage by remember { mutableIntStateOf(0) }
    val orientation = LocalConfiguration.current.orientation
    val isVerticalOrientation =
        orientation == Configuration.ORIENTATION_PORTRAIT || orientation == Configuration.ORIENTATION_UNDEFINED
    val contentAlignment = if (isVerticalOrientation) Alignment.CenterEnd else Alignment.BottomEnd

    if (autoSkip) {
        LaunchedEffect(isPlaying) {
            if (isPlaying && percentage >= 0) {
                while (percentage < 100) {
                    percentage++
                    delay(40L)
                }
                onTimeout()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp, 64.dp),
        contentAlignment = contentAlignment
    ) {
        val endPadding = if (isVerticalOrientation) 0.dp else 16.dp

        OutlinedButton(
            modifier = Modifier.padding(0.dp, 128.dp, endPadding, 0.dp),
            border = BorderStroke(1.dp, Color.White),
            shape = RoundedCornerShape(40),
            contentPadding = PaddingValues(0.dp),
            onClick = onClick
        ) {
            Row(
                modifier = Modifier
                    .drawBehind {
                        if (autoSkip) {
                            drawRoundRect(
                                color = Color.Gray.copy(alpha = 0.5f),
                                size = size.copy(
                                    size.width
                                        .div(100)
                                        .times(percentage)
                                )
                            )
                        }
                    }
                    .padding(18.dp, 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = if (autoSkip) R.string.watch else R.string.opening_skip),
                    color = Color.White
                )
            }
        }
    }
}