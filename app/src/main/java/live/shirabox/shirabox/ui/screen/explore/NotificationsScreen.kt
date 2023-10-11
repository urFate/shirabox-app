package live.shirabox.shirabox.ui.screen.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import live.shirabox.shirabox.R
import live.shirabox.shirabox.ui.component.general.ListItem
import live.shirabox.shirabox.ui.component.top.TopBar

@Composable
fun NotificationsScreen(){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopBar(null)

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                modifier = Modifier.padding(16.dp, 0.dp),
                text = stringResource(R.string.notifications),
                fontSize = 22.sp,
                fontWeight = FontWeight(500)
            )
            Text(
                modifier = Modifier.padding(16.dp, 0.dp),
                text = "05.05.2023",
                fontSize = 15.sp,
                fontWeight = FontWeight(500)
            )
            repeat(4) {
                ListItem(
                    headlineContent = {
                        Box(Modifier.fillMaxWidth()) {
                            Text(modifier = Modifier.align(Alignment.TopStart),
                                text = "Название")
                            Text(modifier = Modifier.align(Alignment.CenterEnd),
                                text = "14:32",
                                fontSize = 12.sp)
                        }
                    },
                    supportingString = "Вышла новая серия 10 от Unknown Team!",
                    coverImage = ImageBitmap.imageResource(id = R.drawable.blank)
                ) {
                    /* TODO */
                }
            }
        }
    }
}