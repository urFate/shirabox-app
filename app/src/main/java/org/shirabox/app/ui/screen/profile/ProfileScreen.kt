package org.shirabox.app.ui.screen.profile

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.shirabox.app.R
import org.shirabox.app.ui.activity.settings.SettingsActivity
import org.shirabox.app.ui.activity.update.AppUpdateActivity
import org.shirabox.app.ui.component.navigation.NestedNavItems
import org.shirabox.app.ui.theme.ShiraBoxTheme
import org.shirabox.core.util.Util.Companion.openUri

@Composable
fun ProfileScreen(navController: NavController){
    val context = LocalContext.current
    val site = "https://www.shirabox.org"

    val donationUri = Uri.parse("$site/donate")
    val helpUri = Uri.parse("$site/faq")


    ShiraBoxTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            OutlinedCard(
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .padding(16.dp)
                    .height(72.dp)
                    .clickable {
                        Toast.makeText(context, "В разработке...", Toast.LENGTH_SHORT).show()
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Login,
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
                HorizontalDivider(
                    modifier = Modifier
                        .padding(horizontal = 48.dp, vertical = 8.dp), thickness = 1.dp
                )
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
                    modifier = Modifier.clickable {
                        openUri(context, donationUri)
                    },
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
                    modifier = Modifier.clickable {
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                    },
                    leadingContent = {
                        Icon(
                            Icons.Outlined.Settings,
                            contentDescription = "Settings icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                HorizontalDivider(
                    modifier = Modifier
                        .padding(horizontal = 48.dp, vertical = 8.dp), thickness = 1.dp
                )
                ListItem(
                    headlineContent = { Text(stringResource(id = R.string.updates_check) ) },
                    modifier = Modifier.clickable {
                        context.startActivity(Intent(context, AppUpdateActivity::class.java))
                    },
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
                    modifier = Modifier.clickable {
                        openUri(context, helpUri)
                    },
                    leadingContent = {
                        Icon(
                            Icons.AutoMirrored.Outlined.HelpOutline,
                            contentDescription = "Help icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }
    }
}