package live.shirabox.shirabox.ui.component.general

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Monogram(modifier: Modifier = Modifier, str: String) {
    val background = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    val text = str.replace(Regex("[a-z]"), "")
        .replace(Regex("[а-я]"), "").uppercase().take(2)

    Box(
        modifier = Modifier
            .size(40.dp)
            .drawBehind {
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            background.copy(0.8f),
                            background.copy(0.45f)
                        )
                    ),
                    radius = this.size.width.div(2)
                )
            }
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(color = onPrimary, fontSize = 16.sp),
            fontWeight = FontWeight.Medium
        )
    }
}