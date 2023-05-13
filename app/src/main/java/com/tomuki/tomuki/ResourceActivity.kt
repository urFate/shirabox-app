package com.tomuki.tomuki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomuki.tomuki.ui.theme.BrandBrown
import com.tomuki.tomuki.ui.theme.BrandBrownLight
import com.tomuki.tomuki.ui.theme.TomukiTheme

class ResourceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TomukiTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ShowResource()
                }
            }
        }
    }
}

@Composable
@Preview
fun ShowResource(){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        val gradientColor = if (isSystemInDarkTheme()) BrandBrownLight else BrandBrown
        Box{
            Image(
                modifier = Modifier
                    .clickable { }
                    .padding(top = 0.dp)
                    .fillMaxWidth()
                    .height(512.dp)
                    .drawWithCache {
                        val gradient = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                gradientColor
                            ), // TODO: make white gradient
                            startY = 0f,
                            endY = size.height
                        )
                        onDrawWithContent {
                            drawContent()
                            drawRect(gradient, blendMode = BlendMode.Multiply)
                        }
                    },
                painter = painterResource(id = R.drawable.blank),
                contentDescription = "blank",
                contentScale = ContentScale.FillWidth)
                Image(
                    modifier = Modifier
                        .clickable { }
                        .align(Alignment.Center)
                        .width(215.dp)
                        .height(300.dp)
                        .clip(RoundedCornerShape(32.dp)),
                    painter = painterResource(id = R.drawable.blank),
                    contentDescription = "blank",
                    contentScale = ContentScale.Crop)

            IconButton(
                onClick = { /*TODO*/ },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clickable { }
            ) {
                Icon(Icons.Outlined.MoreVert, "MoreVert Icon")
            }
        }
        Text(
            modifier = Modifier.padding(16.dp, 0.dp),
            text = "Название ресурса",
            fontSize = 22.sp,
            fontWeight = FontWeight.W800
        )
        Text(
            modifier = Modifier.padding(16.dp, 0.dp),
            text = "Оригинальное название",
            fontSize = 15.sp,
            fontWeight = FontWeight.W300
        )
        Button(
            modifier = Modifier
                .padding(16.dp)
                .width(156.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4141)),
            onClick = { /* Do something! */ },
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding

        ){
            Icon( // TODO: сделать размер кнопки по макету
                Icons.Filled.Favorite,
                contentDescription = "Localized description",
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Сохранить")
        }

    }
}