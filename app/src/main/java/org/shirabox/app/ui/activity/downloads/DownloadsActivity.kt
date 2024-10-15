package org.shirabox.app.ui.activity.downloads

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownloadDone
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.shirabox.app.R
import org.shirabox.app.ui.activity.downloads.presentation.DownloadsTab
import org.shirabox.app.ui.activity.downloads.presentation.DownloadsTopBar
import org.shirabox.app.ui.theme.ShiraBoxTheme

class DownloadsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShiraBoxTheme {
                DownloadsScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { DownloadsTopBar() },
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                divider = {
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                tabs = {
                    DownloadsTab(
                        text = stringResource(R.string.downloads_query),
                        icon = Icons.Outlined.FileDownload,
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 }
                    )

                    DownloadsTab(
                        text = stringResource(R.string.downloads_saved),
                        icon = Icons.Filled.FileDownloadDone,
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 }
                    )
                }
            )
        }
    }
}

