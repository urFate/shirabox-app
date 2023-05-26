package com.tomuki.tomuki.ui.activity.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.tomuki.tomuki.ui.theme.TomukiTheme
import com.tomuki.tomuki.util.Util

class PlayerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TomukiTheme(
                darkTheme = false
            ) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // TODO: Test stream
                    val streams = listOf(
                        "https://cache.libria.fun/videos/media/ts/9412/1/480/75818d390a1be66329973dc6a05b3a8a.m3u8",
                        "https://cache.libria.fun/videos/media/ts/9412/2/480/cffe3e3b9d8a40322007ddbbcdd7b808.m3u8",
                        "https://cache.libria.fun/videos/media/ts/9412/3/480/150ea2e6e2c5507063374281de5f370c.m3u8"
                    )

                    val systemUiController = rememberSystemUiController()

                    systemUiController.setStatusBarColor(
                        color = Color.Transparent,
                        darkIcons = false
                    )

                    Util.hideSystemUi(systemUiController)

                    TomuPlayer(
                        title = "Название",
                        itemsUrls = streams
                    )
                }
            }
        }
    }
}