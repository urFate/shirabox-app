package com.tomuki.tomuki.ui.screen.profile

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
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.twotone.Help
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
import com.tomuki.tomuki.R
import com.tomuki.tomuki.ui.component.navigation.ProfileNavItems
import com.tomuki.tomuki.ui.theme.TomukiTheme

@Composable
fun ProfileScreen(navController: NavController){
    TomukiTheme {
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
                        contentDescription = "Login Icon"
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
                    modifier = Modifier.clickable(onClick = { navController.navigate(ProfileNavItems.History.route) }),

                    leadingContent = {
                        Icon(
                            Icons.Filled.History,
                            contentDescription = "History Icon",
                        )
                    }
                )
                ListItem(
                    headlineContent = { Text(stringResource(id = R.string.donate) ) },
                    modifier = Modifier.clickable {  },
                    leadingContent = {
                        Icon(
                            Icons.Filled.Savings,
                            contentDescription = "Savings icon",
                        )
                    }
                )
                ListItem(
                    headlineContent = { Text(stringResource(id = R.string.settings)) },
                    modifier = Modifier.clickable {  },
                    leadingContent = {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Settings icon",
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
                            Icons.Filled.SystemUpdate,
                            contentDescription = "SystemUpdate icon",
                        )
                    }
                )
                ListItem(
                    headlineContent = { Text(stringResource(id = R.string.help)) },
                    modifier = Modifier.clickable {  },
                    leadingContent = {
                        Icon(
                            Icons.TwoTone.Help,
                            contentDescription = "Help icon",
                        )
                    }
                )
            }
        }
    }
}