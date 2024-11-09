package org.shirabox.app.ui.activity.downloads

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.shirabox.app.ui.activity.downloads.presentation.DownloadsTab
import org.shirabox.app.ui.activity.downloads.presentation.DownloadsTopBar
import org.shirabox.app.ui.theme.ShiraBoxTheme

@AndroidEntryPoint
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
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState { DownloadsTabsItems.items.size }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { DownloadsTopBar() },
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            PrimaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                divider = {
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                tabs = {
                    DownloadsTabsItems.items.forEachIndexed { index, item ->
                        DownloadsTab(
                            text = stringResource(item.name),
                            icon = item.icon,
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.scrollToPage(index)
                                }
                            }
                        )
                    }
                }
            )

            HorizontalPager(
                state = pagerState
            ) { index ->
                DownloadsTabsItems.items[index].content()
            }
        }
    }
}

