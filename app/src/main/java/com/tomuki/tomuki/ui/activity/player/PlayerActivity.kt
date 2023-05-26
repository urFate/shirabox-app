package com.tomuki.tomuki.ui.activity.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
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
                    val stream = remember {
                        "https://cache.libria.fun/videos/media/ts/9000/1/480" +
                                "/8a7f4d218433f5a5fee1c6f5a02d278e.m3u8"
                    }

                    val systemUiController = rememberSystemUiController()

                    systemUiController.setStatusBarColor(
                        color = Color.Transparent,
                        darkIcons = false
                    )

                    Util.hideSystemUi(systemUiController)

                    TomuPlayer(stream)
                }
            }
        }
    }
}