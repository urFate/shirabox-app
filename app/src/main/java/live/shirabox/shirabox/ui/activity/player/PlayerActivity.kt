package live.shirabox.shirabox.ui.activity.player

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
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
import kotlinx.serialization.json.Json
import live.shirabox.core.util.Util
import live.shirabox.shirabox.ui.theme.ShiraBoxTheme

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
                    val activity = context as? Activity

                    rememberSystemUiController().apply {
                        setStatusBarColor(
                            color = Color.Transparent,
                            darkIcons = false
                        )
                        Util.hideSystemUi(this)
                    }

                    lateinit var model: PlayerViewModel

                    try {
                        model = PlayerViewModel(
                            context = context,
                            contentUid = arguments!!.getLong("content_uid"),
                            contentName = arguments.getString("name").toString(),
                            episode = arguments.getInt("episode"),
                            startIndex = arguments.getInt("start_index"),
                            playlist = Json.decodeFromString(
                                arguments.getString("playlist") ?: ""
                            )
                        )
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        activity?.finish()
                        Toast.makeText(context, ex.localizedMessage, Toast.LENGTH_LONG).show()
                    }

                    ShiraPlayer(model = viewModel(factory = Util.viewModelFactory { model }))
                }
            }
        }
    }
}