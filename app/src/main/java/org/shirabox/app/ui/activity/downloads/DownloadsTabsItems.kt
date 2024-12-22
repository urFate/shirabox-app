package org.shirabox.app.ui.activity.downloads

import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.rounded.FileDownloadDone
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import org.shirabox.app.R
import org.shirabox.app.ui.activity.downloads.screen.DownloadsPausedScreen
import org.shirabox.app.ui.activity.downloads.screen.DownloadsQueryScreen
import org.shirabox.app.ui.activity.downloads.screen.DownloadsSavedScreen

sealed class DownloadsTabsItems(
    val name: Int,
    val icon: ImageVector,
    val content: @Composable (pagerState: PagerState) -> Unit
) {
    companion object {
        val items = listOf(QueryTab, SuspendedTab, SavedTab)
    }

    data object QueryTab :
        DownloadsTabsItems(
            name = R.string.downloads_query,
            icon = Icons.Outlined.FileDownload,
            content = { DownloadsQueryScreen(pagerState = it) }
        )

    data object SuspendedTab :
        DownloadsTabsItems(
            name = R.string.downloads_suspended,
            icon = Icons.Rounded.Pause,
            content = { DownloadsPausedScreen(pagerState = it) }
        )

    data object SavedTab :
        DownloadsTabsItems(
            name = R.string.downloads_saved,
            icon = Icons.Rounded.FileDownloadDone,
            content = { DownloadsSavedScreen() }
        )
}