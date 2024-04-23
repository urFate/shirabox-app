package live.shirabox.shirabox.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import live.shirabox.shirabox.R
import live.shirabox.shirabox.ui.component.navigation.BottomNavigationView
import live.shirabox.shirabox.ui.screen.explore.notifications.NotificationsDialog
import live.shirabox.shirabox.ui.theme.ShiraBoxTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_ShiraBox)

        setContent {
            ShiraBoxTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NotificationsDialog()
                    BottomNavigationView()
                }
            }

            enableEdgeToEdge()
        }
    }
}