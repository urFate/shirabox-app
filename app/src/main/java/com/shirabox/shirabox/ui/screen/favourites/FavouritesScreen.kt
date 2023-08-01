package com.shirabox.shirabox.ui.screen.favourites

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.shirabox.shirabox.R
import com.shirabox.shirabox.model.ContentType
import com.shirabox.shirabox.ui.component.top.TopBar
import com.shirabox.shirabox.ui.component.top.navigation.MediaNavBar

@Composable
fun FavouritesScreen(navController: NavController){

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
            modifier = Modifier.padding(16.dp, 0.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.favourites),
                fontSize = 22.sp,
                fontWeight = FontWeight(500)
            )

            LazyVerticalGrid(
                modifier = Modifier.height((180*4).dp),
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                userScrollEnabled = false
            ){
                items(4) {
                    Surface(
                        modifier = Modifier.size(180.dp, 240.dp),
                        shape = RoundedCornerShape(10)

                    ) {
                        Image(
                            modifier = Modifier.clickable {  },
                            painter = painterResource(id = R.drawable.blank),
                            contentDescription = "blank",
                            contentScale = ContentScale.Crop)
                    }
                }
            }
        }
    }
}