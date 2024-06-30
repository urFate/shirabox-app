package org.shirabox.app.ui.component.top

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import org.shirabox.app.R
import org.shirabox.app.ui.activity.search.SearchActivity
import org.shirabox.app.ui.component.navigation.NestedNavItems
import org.shirabox.app.ui.screen.explore.notifications.NotificationsViewModel
import org.shirabox.core.util.Util

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    navController: NavController,
    model: NotificationsViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val notifications = model.allNotificationsFlow().catch {
        it.printStackTrace()
        emitAll(emptyFlow())
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    Row(
        modifier = Modifier
            .padding(16.dp, 16.dp, 16.dp, 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(weight = 1f, fill = false)
                .height(SearchBarDefaults.InputFieldHeight)
                .clip(SearchBarDefaults.dockedShape)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .clickable {
                    context.startActivity(
                        Intent(
                            context,
                            SearchActivity::class.java
                        )
                    )
                },
        ) {
            Row(
                modifier = Modifier.padding(TextFieldDefaults.contentPaddingWithoutLabel()),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search icon")
                Text(text = stringResource(id = R.string.search_by_name))
            }
        }

        IconButton(
            modifier = Modifier.requiredSize(48.dp),
            onClick = { navController.navigate(NestedNavItems.Notifications.route) }
        ) {
            BadgedBox(
                badge = {
                    if (notifications.value.isNotEmpty()) Badge {
                        Text(text = Util.formatBadgeNumber(notifications.value.size))
                }
            }) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Rounded.NotificationsNone,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

        }
    }
}