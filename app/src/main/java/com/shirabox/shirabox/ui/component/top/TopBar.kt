package com.shirabox.shirabox.ui.component.top

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.NotificationsNone
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
import androidx.navigation.NavController
import com.shirabox.shirabox.R
import com.shirabox.shirabox.ui.activity.search.SearchActivity
import com.shirabox.shirabox.ui.component.navigation.NestedNavItems

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavController?) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .padding(16.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(weight = 1f, fill = false)
                .height(SearchBarDefaults.InputFieldHeight)
                .clip(SearchBarDefaults.dockedShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
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
            onClick = { navController?.navigate(NestedNavItems.Notifications.route) }
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = Icons.Rounded.NotificationsNone,
                contentDescription = "Notifications",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}