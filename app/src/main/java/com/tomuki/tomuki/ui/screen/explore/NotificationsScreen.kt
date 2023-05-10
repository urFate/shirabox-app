package com.tomuki.tomuki.ui.screen.explore

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomuki.tomuki.R
import com.tomuki.tomuki.ui.component.general.CardListItem
import com.tomuki.tomuki.ui.component.top.MediaTypesBar
import com.tomuki.tomuki.ui.component.top.TopBar

@Composable
@Preview
fun NotificationsScreen(){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopBar(hint = stringResource(R.string.search_by_name), null)
        MediaTypesBar()

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
                CardListItem(
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