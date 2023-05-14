package com.tomuki.tomuki

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.MovieCreation
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun ShowResource(colorScheme: ColorScheme = MaterialTheme.colorScheme){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Box {
            val activity = (LocalContext.current as? Activity)
            Image(
                modifier = Modifier
                    .padding(top = 0.dp)
                    .fillMaxWidth()
                    .height(512.dp)
                    .graphicsLayer(alpha = 0.99f)
                    .drawWithCache {
                        onDrawWithContent {
                            drawContent()
                            drawRect(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(
                                            colorScheme.background.red,
                                            colorScheme.background.green,
                                            colorScheme.background.blue,
                                            0.7f
                                        ),
                                        colorScheme.background
                                    )
                                ), blendMode = BlendMode.SrcAtop
                            )
                        }
                    },
                painter = painterResource(id = R.drawable.blank),
                contentDescription = "blank",
                contentScale = ContentScale.FillWidth
            )

            TopAppBar (
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = { activity?.finish() },
                    ) {
                        Icon(Icons.Outlined.ArrowBack, "MoreVert Icon")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(Color.Transparent),
                actions = {
                    IconButton(
                        onClick = { /*TODO*/ },
                    ) {
                        Icon(Icons.Outlined.MoreVert, "MoreVert Icon")
                    }
                },
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
            )

            Image(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(215.dp)
                    .height(300.dp)
                    .clip(RoundedCornerShape(32.dp)),
                painter = painterResource(id = R.drawable.blank),
                contentDescription = "blank",
                contentScale = ContentScale.Crop
            )
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

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp))
        {
            Button(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(55.dp)
                    .weight(weight = 1f, fill = false),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4141)),
                onClick = { /* Do something! */ },
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding

            ){
                Icon(
                    Icons.Outlined.Favorite,
                    contentDescription = "Localized description",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Сохранить")
            }

            Button(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(55.dp)
                    .weight(weight = 1f, fill = false),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.surfaceTint),
                onClick = { /* Do something! */ },
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding

            ){
                Icon(
                    Icons.Outlined.PlayArrow,
                    contentDescription = "Localized description",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Смотреть")
            }
        }

        Divider(thickness = 1.dp,
            modifier = Modifier
                .padding(horizontal = 48.dp, vertical = 8.dp))

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ResourceDataLabel(icon = Icons.Outlined.MovieCreation, text = "Unknown Studio, 1997")
            ResourceDataLabel(icon = Icons.Outlined.EventAvailable, text = "Сериал, выпуск продолжается")
            ResourceDataLabel(icon = Icons.Outlined.LiveTv, text = "0 из 12 эп. по ~20 мин.")
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(start = 16.dp)
        ){
            repeat(4){
                InputChip(
                    selected = true,
                    label = {
                        Text("Жанр $it",
                            fontWeight = FontWeight.W500,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp)
                    },
                    onClick = { /*TODO*/ })
            }
        }
    }
}

@Composable
fun ResourceDataLabel(icon: ImageVector, text: String){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

