package com.tomuki.tomuki.ui.screen.explore

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.tomuki.tomuki.R
import com.tomuki.tomuki.ui.activity.ResourceActivity
import com.tomuki.tomuki.ui.activity.player.PlayerActivity
import com.tomuki.tomuki.ui.component.top.MediaTypesBar
import com.tomuki.tomuki.ui.component.top.TopBar

@ExperimentalMaterial3Api
@Composable
fun ExploreScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val context = LocalContext.current

        TopBar(stringResource(R.string.search_by_name), navController)
        MediaTypesBar()

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                modifier = Modifier.padding(16.dp, 0.dp),
                text = stringResource(R.string.anime_actual),
                fontSize = 22.sp,
                fontWeight = FontWeight(500)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(4){
                    if(it == 0) Spacer(modifier = Modifier.width(16.dp))

                    Surface(
                        modifier = Modifier.size(160.dp, 220.dp),
                        shape = RoundedCornerShape(20)

                    ) {
                        Image(
                            modifier = Modifier.clickable { context.startActivity(Intent(context, ResourceActivity::class.java)) },
                            painter = painterResource(id = R.drawable.blank),
                            contentDescription = "blank",
                            contentScale = ContentScale.Crop)
                    }
                }
            }
        }

        Divider(
            modifier = Modifier.padding(32.dp, 12.dp)
        )

        Column(
            modifier = Modifier.padding(16.dp, 0.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.popular),
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
                            modifier = Modifier.clickable {
                                context.startActivity(Intent(context, PlayerActivity::class.java))
                            },
                            painter = painterResource(id = R.drawable.blank),
                            contentDescription = "blank",
                            contentScale = ContentScale.Crop)
                    }
                }
            }
        }
    }
}

