package org.shirabox.app.ui.activity.settings

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.shirabox.app.R
import org.shirabox.app.ui.theme.ShiraBoxTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShiraBoxTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Settings()
                }
            }

            enableEdgeToEdge()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(
    items: List<SettingsNavItems> = settingsNavItems
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val activity = context as? Activity

    val currentEntry = navController.currentBackStackEntryAsState()
    val currentRouteName = remember(currentEntry.value) {
        items.findLast { it.route == currentEntry.value?.destination?.route }?.name
    }

    Column {
        TopAppBar(
            title = { Text(stringResource(currentRouteName ?: R.string.settings)) },
            navigationIcon = {
                IconButton(
                    onClick = {
                        if (!navController.popBackStack()) activity?.finish()
                    },
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        SettingsNavHost(navController = navController)
    }
}