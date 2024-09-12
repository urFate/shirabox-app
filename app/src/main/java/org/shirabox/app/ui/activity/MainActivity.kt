package org.shirabox.app.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import org.shirabox.app.R
import org.shirabox.app.ui.component.navigation.base.BottomNavigationView
import org.shirabox.app.ui.screen.explore.notifications.NotificationsDialog
import org.shirabox.app.ui.theme.ShiraBoxTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_ShiraBox)

        setContent {
            ShiraBoxTheme(
                transparentStatusBar = true
            ) {
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