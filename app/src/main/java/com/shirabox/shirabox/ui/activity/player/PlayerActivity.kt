package com.shirabox.shirabox.ui.activity.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.shirabox.shirabox.ui.theme.ShiraBoxTheme
import com.shirabox.shirabox.util.Util
import kotlinx.serialization.json.Json

class PlayerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ShiraBoxTheme(
                darkTheme = false
            ) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val arguments = intent.extras
                    val context = LocalContext.current

                    rememberSystemUiController().apply {
                        setStatusBarColor(
                            color = Color.Transparent,
                            darkIcons = false
                        )
                        Util.hideSystemUi(this)
                    }


                    val model: PlayerViewModel = viewModel(factory = Util.viewModelFactory {
                        PlayerViewModel(
                            context = context,
                            contentUid = arguments?.getInt("content_uid") ?: -1,
                            contentName = arguments?.getString("name").toString(),
                            startEpisode = arguments?.getInt("episode") ?: 0,
                            playlist = Json.decodeFromString(
                                arguments?.getString("playlist") ?: ""
                            )
                        )
                    })

                    ShiraPlayer(model = model)
                }
            }
        }
    }
}