package com.tomuki.tomuki.ui.screen.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomuki.tomuki.R
import com.tomuki.tomuki.ui.component.top.MediaTypesBar
import com.tomuki.tomuki.ui.component.top.TopBar

@Composable
@Preview
fun History(){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopBar(hint = stringResource(R.string.search_in_history))
        MediaTypesBar()

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
            ListItem(
                modifier = Modifier.clickable {  },
                headlineContent = { Text("Название ресурса 3") },
                supportingContent = { Text("Серия 12 - 19:04") },
                trailingContent = { Icon(Icons.Outlined.Delete, "Delete icon")},
                leadingContent = {
                    Image(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .height(72.dp)
                            .width(54.dp),
                        painter = painterResource(id = R.drawable.blank),
                        contentDescription = "blank",
                        contentScale = ContentScale.Crop)
                }
            )
            ListItem(
                modifier = Modifier.clickable {  },
                headlineContent = { Text("Название ресурса 2") },
                supportingContent = { Text("Серия 3 - 13:34") },
                trailingContent = { Icon(Icons.Outlined.Delete, "Delete icon")},
                leadingContent = {
                    Image(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .height(72.dp)
                            .width(54.dp),
                        painter = painterResource(id = R.drawable.blank),
                        contentDescription = "blank",
                        contentScale = ContentScale.Crop)
                }
            )
            Text(
                    modifier = Modifier.padding(16.dp, 0.dp),
                text = "28.04.2023",
                fontSize = 15.sp,
                fontWeight = FontWeight(500)
            )
            ListItem(
                modifier = Modifier.clickable {  },
                headlineContent = { Text("Название ресурса 1") },
                supportingContent = { Text("Серия 1 - 06058") },
                trailingContent = { Icon(Icons.Outlined.Delete, "Delete icon")},
                leadingContent = {
                    Image(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .height(72.dp)
                            .width(54.dp),
                        painter = painterResource(id = R.drawable.blank),
                        contentDescription = "blank",
                        contentScale = ContentScale.Crop)
                }
            )
        }
    }
}