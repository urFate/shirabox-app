package live.shirabox.shirabox.ui.activity.settings.category

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import live.shirabox.core.util.Util
import live.shirabox.core.util.Util.Companion.openUri
import live.shirabox.shirabox.BuildConfig
import live.shirabox.shirabox.R

@Composable
fun AboutSettingsScreen() {
    val context = LocalContext.current
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    val site = "https://www.shirabox.live"

    val privacyPolicyUri = Uri.parse("$site/privacy")
    val licenceUri = Uri.parse("https://github.com/urFate/shirabox-app/blob/master/LICENSE")
    val siteUri = Uri.parse(site)
    val githubUri = Uri.parse("$site/github")
    val telegramUri = Uri.parse("$site/telegram")
    val versionString = "${Util.getAppVersion(context)} (${BuildConfig.BUILD_TYPE})"

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(painter = painterResource(id = R.drawable.shirabox), contentDescription = "Logo")
        }
        HorizontalDivider(Modifier.padding(24.dp))

        ListItem(
            modifier = Modifier.clickable {
                clipboard.setPrimaryClip(ClipData.newPlainText("ShiraBox Version", versionString))
            },
            headlineContent = {
                Text(
                    modifier = Modifier.padding(16.dp, 0.dp),
                    text = stringResource(id = R.string.version)
                )
            },
            supportingContent = {
                Text(
                    modifier = Modifier.padding(16.dp, 0.dp),
                    text = versionString
                )
            }
        )

        ListItem(
            modifier = Modifier.clickable { openUri(context, licenceUri) },
            headlineContent = {
                Text(
                    modifier = Modifier.padding(16.dp, 0.dp),
                    text = stringResource(id = R.string.license)
                )
            }
        )

        ListItem(
            modifier = Modifier.clickable { openUri(context, privacyPolicyUri) },
            headlineContent = {
                Text(
                    modifier = Modifier.padding(16.dp, 0.dp),
                    text = stringResource(id = R.string.privacy_policy)
                )
            },
        )

        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = { openUri(context, siteUri) }) {
                Icon(
                    modifier = Modifier.size(26.dp),
                    imageVector = Icons.Outlined.Public,
                    contentDescription = "website"
                )
            }
            IconButton(onClick = { openUri(context, githubUri) }) {
                Icon(
                    modifier = Modifier.size(26.dp),
                    painter = painterResource(id = R.drawable.github),
                    contentDescription = "github"
                )
            }
            IconButton(onClick = { openUri(context, telegramUri) }) {
                Icon(
                    modifier = Modifier.size(26.dp),
                    painter = painterResource(id = R.drawable.telegram),
                    contentDescription = "website"
                )
            }
        }
    }
}