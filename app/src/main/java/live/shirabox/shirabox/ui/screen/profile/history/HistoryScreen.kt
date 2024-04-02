package live.shirabox.shirabox.ui.screen.profile.history

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import live.shirabox.core.model.ContentType
import live.shirabox.core.util.Util
import live.shirabox.shirabox.R
import live.shirabox.shirabox.ui.activity.resource.ResourceActivity
import live.shirabox.shirabox.ui.component.general.DespondencyEmoticon
import live.shirabox.shirabox.ui.component.general.ListItem
import live.shirabox.shirabox.ui.component.top.TopBar
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun History(
    navController: NavController,
    model: HistoryViewModel = viewModel(factory = Util.viewModelFactory {
        HistoryViewModel(context = navController.context.applicationContext)
    })
) {
    val context = LocalContext.current
    val contents by model.contentsFlow().collectAsState(initial = emptyList())

    var currentType by remember {
        mutableStateOf(ContentType.ANIME)
    }

    val filteredContentsByTypeAndDate by remember(currentType) {
        derivedStateOf {
            val languageCode = Locale.current.language

            contents.reversed().filter { it.type == currentType }.groupBy {
                SimpleDateFormat(
                    "dd.MM.yyyy",
                    java.util.Locale(languageCode)
                ).format(Date(it.lastViewTimestamp))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopBar(navController)

        Text(
            modifier = Modifier.padding(16.dp, 0.dp),
            text = stringResource(R.string.history),
            fontSize = 22.sp,
            fontWeight = FontWeight(500)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (filteredContentsByTypeAndDate.isEmpty())
                DespondencyEmoticon(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.empty_library)
                )

            filteredContentsByTypeAndDate.forEach { listEntry ->
                Text(
                    modifier = Modifier.padding(16.dp, 0.dp),
                    text = listEntry.key,
                    fontSize = 15.sp,
                    fontWeight = FontWeight(500)
                )

                listEntry.value.forEach { contentEntity ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = contentEntity.name,
                                fontWeight = FontWeight.Medium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        supportingString = "${contentEntity.production?.let { "$it\n" }}" +
                                "${contentEntity.releaseYear}, ${contentEntity.kind}",
                        coverImage = contentEntity.image
                    ) {
                        context.startActivity(
                            Intent(
                                context,
                                ResourceActivity::class.java
                            ).apply {
                                putExtra("id", contentEntity.shikimoriID)
                                putExtra("type", contentEntity.type)
                            }
                        )
                    }
                }
            }
        }
    }
}