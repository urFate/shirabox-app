package com.shirabox.shirabox.ui.screen.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.shirabox.shirabox.R
import com.shirabox.shirabox.ui.component.navigation.NestedNavItems
import com.shirabox.shirabox.ui.theme.ShiraBoxTheme

@Composable
fun ProfileScreen(navController: NavController){
    ShiraBoxTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            OutlinedCard(
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth(1f) // Set the desired width percentage
                    .padding(16.dp)
                    .height(72.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Login,
                        contentDescription = "Login Icon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(id = R.string.unauthorized),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = stringResource(id = R.string.unauthorized_suggestion),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            Column {
                Divider(thickness = 1.dp,
                    modifier = Modifier
                        .padding(horizontal = 48.dp, vertical = 8.dp))
                ListItem(
                    headlineContent = { Text(stringResource(R.string.history)) },
                    modifier = Modifier.clickable(onClick = {
                        navController.navigate(NestedNavItems.History.route) }),

                    leadingContent = {
                        Icon(
                            Icons.Filled.History,
                            contentDescription = "History Icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                ListItem(
                    headlineContent = { Text(stringResource(id = R.string.donate) ) },
                    modifier = Modifier.clickable {  },
                    leadingContent = {
                        Icon(
                            Icons.Outlined.Savings,
                            contentDescription = "Savings icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                ListItem(
                    headlineContent = { Text(stringResource(id = R.string.settings)) },
                    modifier = Modifier.clickable {  },
                    leadingContent = {
                        Icon(
                            Icons.Outlined.Settings,
                            contentDescription = "Settings icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                Divider(thickness = 1.dp,
                modifier = Modifier
                    .padding(horizontal = 48.dp, vertical = 8.dp))
                ListItem(
                    headlineContent = { Text(stringResource(id = R.string.updates_check) ) },
                    modifier = Modifier.clickable {  },
                    leadingContent = {
                        Icon(
                            Icons.Outlined.SystemUpdate,
                            contentDescription = "SystemUpdate icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                ListItem(
                    headlineContent = { Text(stringResource(id = R.string.help)) },
                    modifier = Modifier.clickable {  },
                    leadingContent = {
                        Icon(
                            Icons.Outlined.HelpOutline,
                            contentDescription = "Help icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }
    }
}