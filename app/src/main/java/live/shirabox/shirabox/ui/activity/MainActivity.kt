package live.shirabox.shirabox.ui.activity

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.preferencesDataStore
import live.shirabox.shirabox.ui.activity.settings.SettingsScheme
import live.shirabox.shirabox.ui.activity.settings.dataStoreEmptyStateFlow
import live.shirabox.shirabox.ui.activity.settings.resetDataStore
import live.shirabox.shirabox.ui.component.navigation.BottomNavigationView
import live.shirabox.shirabox.ui.theme.ShiraBoxTheme

val Context.dataStore by preferencesDataStore(
    name = SettingsScheme.DATASTORE_NAME
)

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShiraBoxTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current

                    val dataStoreEmptyStateFlow = dataStoreEmptyStateFlow(context).collectAsState(
                        initial = false
                    )

                    // Set defaults if datastore is empty
                    LaunchedEffect(dataStoreEmptyStateFlow) {
                        if(dataStoreEmptyStateFlow.value) resetDataStore(context)
                    }

                    BottomNavigationView()
                }
            }
        }
    }
}