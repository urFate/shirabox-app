package org.shirabox.app.ui.activity.downloads

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import org.shirabox.app.R
import org.shirabox.app.ui.activity.downloads.screen.DownloadsPausedScreen
import org.shirabox.app.ui.activity.downloads.screen.DownloadsQueryScreen
import org.shirabox.app.ui.activity.downloads.screen.DownloadsSavedScreen

sealed class DownloadsTabsItems(
    val name: Int,
    val icon: Int,
    val content: @Composable (pagerState: PagerState) -> Unit
) {
    companion object {
        val items = listOf(QueryTab, SuspendedTab, SavedTab)
    }

    data object QueryTab :
        DownloadsTabsItems(
            name = R.string.downloads_query,
            icon = R.drawable.download,
            content = { DownloadsQueryScreen(pagerState = it) }
        )

    data object SuspendedTab :
        DownloadsTabsItems(
            name = R.string.downloads_suspended,
            icon = R.drawable.pause,
            content = { DownloadsPausedScreen(pagerState = it) }
        )

    data object SavedTab :
        DownloadsTabsItems(
            name = R.string.downloads_saved,
            icon = R.drawable.hard_drive,
            content = { DownloadsSavedScreen() }
        )
}