package live.shirabox.shirabox.ui.screen.explore.notifications

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import live.shirabox.core.util.Util
import live.shirabox.shirabox.R
import live.shirabox.shirabox.ui.activity.resource.ResourceActivity
import live.shirabox.shirabox.ui.component.general.DespondencyEmoticon
import live.shirabox.shirabox.ui.component.general.ListItem
import live.shirabox.shirabox.ui.component.top.TopBar
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun NotificationsScreen(
    navController: NavController,
    model: NotificationsViewModel = viewModel(factory = Util.viewModelFactory {
        NotificationsViewModel(context = navController.context.applicationContext)
    })
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopBar(null)
        val context = LocalContext.current

        val notifications = model.fetchNotifications().collectAsState(initial = emptyList())

        val filteredNotificationsByDate by remember(model.notificationsWithContent) {
            derivedStateOf {
                val languageCode = Locale.current.language

                model.notificationsWithContent.groupBy {
                    SimpleDateFormat(
                        "dd.MM.yyyy",
                        java.util.Locale(languageCode)
                    ).format(Date(it.notificationEntity.receiveTimestamp))
                }
            }
        }
        
        LaunchedEffect(notifications) {
            model.fetchNotificationsWithContent(
                notifications.value.map { it.contentEnName }
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp, 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.notifications),
                    fontSize = 22.sp,
                    fontWeight = FontWeight(500)
                )

                Button(
                    enabled = notifications.value.isNotEmpty(),
                    onClick = {
                        model.clearNotifications()
                    }
                ) {
                    Text(stringResource(id = R.string.clear_notifications))
                }
            }

            if(notifications.value.isEmpty()) {
                DespondencyEmoticon(text = stringResource(id = R.string.no_notifications))
            }
            
            filteredNotificationsByDate.forEach { entry ->
                Text(
                    modifier = Modifier.padding(16.dp, 0.dp),
                    text = entry.key,
                    fontSize = 15.sp,
                    fontWeight = FontWeight(500)
                )

                entry.value.forEach {
                    val languageCode = Locale.current.language

                    val time = SimpleDateFormat(
                        "H:mm",
                        java.util.Locale(languageCode)
                    ).format(Date(it.notificationEntity.receiveTimestamp))

                    ListItem(
                        headlineContent = {
                            Box(Modifier.fillMaxWidth()) {
                                Text(modifier = Modifier.align(Alignment.TopStart),
                                    text = it.contentEntity.name)
                                Text(modifier = Modifier.align(Alignment.CenterEnd),
                                    text = time,
                                    fontSize = 12.sp)
                            }
                        },
                        supportingString = it.notificationEntity.text,
                        coverImage = it.contentEntity.image
                    ) {
                        context.startActivity(
                            Intent(
                                context,
                                ResourceActivity::class.java
                            ).apply {
                                putExtra("id", it.contentEntity.shikimoriID)
                                putExtra("type", it.contentEntity.type)
                            }
                        )
                    }
                }
            }
        }
    }
}