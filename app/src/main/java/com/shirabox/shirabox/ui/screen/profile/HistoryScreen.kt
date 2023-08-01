package com.shirabox.shirabox.ui.screen.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.shirabox.shirabox.R
import com.shirabox.shirabox.model.ContentType
import com.shirabox.shirabox.ui.component.general.ListItem
import com.shirabox.shirabox.ui.component.top.TopBar
import com.shirabox.shirabox.ui.component.top.navigation.MediaNavBar

@Composable
fun History(navController: NavController){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopBar(navController)

        MediaNavBar(activeType = ContentType.ANIME) {
            TODO("Add database functionality")
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                modifier = Modifier.padding(16.dp, 0.dp),
                text = stringResource(R.string.history),
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
                ListItem(headlineString = "Название", supportingString = "Серия 12 - 19:04",
                    coverImage = ImageBitmap.imageResource(id = R.drawable.blank),
                    trailingIcon = Icons.Outlined.Delete,
                    onClick = {},
                    onTrailingIconClick = {}
                )
            }
        }
    }
}