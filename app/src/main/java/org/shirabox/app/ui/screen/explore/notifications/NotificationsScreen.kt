package org.shirabox.app.ui.screen.explore.notifications

import android.content.Intent
import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import org.shirabox.app.R
import org.shirabox.app.ui.activity.resource.ResourceActivity
import org.shirabox.app.ui.component.general.ListItem
import org.shirabox.app.ui.component.top.TopBar
import org.shirabox.core.model.ContentType
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun NotificationsScreen(
    navController: NavController,
    model: NotificationsViewModel = hiltViewModel()
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopBar(navController)
        val context = LocalContext.current

        val notifications = model.allNotificationsFlow().catch {
            it.printStackTrace()
            emitAll(emptyFlow())
        }.collectAsStateWithLifecycle(initialValue = emptyList())

        val filteredNotificationsByDate = remember(notifications.value) {
            derivedStateOf {
                notifications.value
                    .sortedByDescending { it.receiveTimestamp }
                    .groupBy {
                        when (DateUtils.isToday(it.receiveTimestamp)) {
                            true -> context.resources.getString(R.string.today)
                            false -> DateUtils.formatSameDayTime(
                                it.receiveTimestamp,
                                System.currentTimeMillis(),
                                DateFormat.SHORT,
                                DateFormat.SHORT
                            ).toString()
                        }
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.notifications),
                    fontSize = 22.sp,
                    fontWeight = FontWeight(500)
                )

                OutlinedButton(
                    enabled = notifications.value.isNotEmpty(),
                    onClick = {
                        model.clearNotifications()
                    }
                ) {
                    Text(stringResource(id = R.string.clear_notifications))
                }
            }

            if(notifications.value.isEmpty()) {
                Column(
                    modifier = Modifier
                        .padding(64.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        modifier = Modifier.size(64.dp),
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "awesome",
                        tint = MaterialTheme.colorScheme.surfaceTint.copy(0.4f)
                    )
                    Text(
                        text = stringResource(id = R.string.no_notifications),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            filteredNotificationsByDate.value.forEach { entry ->
                Text(
                    modifier = Modifier
                        .padding(16.dp, 0.dp)
                        .fillMaxWidth(),
                    text = entry.key,
                    textAlign = TextAlign.Left,
                    fontSize = 15.sp,
                    fontWeight = FontWeight(500)
                )

                entry.value.forEach {
                    val languageCode = Locale.current.language

                    val time = SimpleDateFormat(
                        "H:mm",
                        java.util.Locale(languageCode)
                    ).format(Date(it.receiveTimestamp))

                    ListItem(
                        headlineContent = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    modifier = Modifier
                                        .padding(0.dp, 0.dp, 32.dp, 0.dp)
                                        .weight(weight = 1f, fill = false), text = it.title
                                )
                                Text(
                                    text = time,
                                    fontSize = 12.sp
                                )
                            }
                        },
                        supportingString = it.body,
                        coverImage = it.thumbnailUrl
                    ) {
                        context.startActivity(
                            Intent(
                                context,
                                ResourceActivity::class.java
                            ).apply {
                                putExtra("id", it.contentShikimoriId)
                                putExtra("type", ContentType.ANIME)
                            }
                        )
                        model.removeNotification(it)
                    }
                }
            }
        }
    }
}