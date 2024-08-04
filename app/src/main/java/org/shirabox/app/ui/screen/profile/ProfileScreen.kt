package org.shirabox.app.ui.screen.profile

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.shirabox.app.R
import org.shirabox.app.ui.activity.settings.SettingsActivity
import org.shirabox.app.ui.activity.update.AppUpdateActivity
import org.shirabox.app.ui.component.navigation.base.NestedNavItems
import org.shirabox.app.ui.theme.ShiraBoxTheme
import org.shirabox.core.util.Util.Companion.openUri

@Composable
fun ProfileScreen(navController: NavController){
    val context = LocalContext.current
    val site = "https://shirabox.org"

    val donationUri = Uri.parse("$site/donate")
    val helpUri = Uri.parse("$site/faq")

    ShiraBoxTheme {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(0.dp, 4.dp).clickable {
                        Toast.makeText(context, "В разработке...", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(100)),
                            painter = painterResource(id = R.drawable.ic_profile_guest),
                            contentDescription = "Login Icon"
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = stringResource(id = R.string.guest),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = stringResource(id = R.string.unauthorized_suggestion),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            item {
                HorizontalDivider(
                    modifier = Modifier
                        .padding(horizontal = 48.dp, vertical = 8.dp), thickness = 1.dp
                )
            }

            item {
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
            }

            item {
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
            }

            item {
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
            }

            item {
                HorizontalDivider(
                    modifier = Modifier
                        .padding(horizontal = 48.dp, vertical = 8.dp), thickness = 1.dp
                )
            }

            item {
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
            }

            item {
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