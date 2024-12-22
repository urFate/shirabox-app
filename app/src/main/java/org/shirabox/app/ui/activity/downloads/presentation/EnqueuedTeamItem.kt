package org.shirabox.app.ui.activity.downloads.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EnqueuedTeamItem(
    modifier: Modifier = Modifier,
    title: String,
    team: String,
    tasksAmount: Int,
    finished: Int?,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(16.dp, 0.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column (
                modifier = Modifier.weight(weight = 1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = team,
                    fontSize = 12.sp
                )
            }

            if (finished != null) {
                Row(
                    modifier = Modifier.padding(16.dp, 0.dp)
                ) {
                    Text(
                        text = "${finished}/${tasksAmount}",
                        fontSize = 14.sp
                    )
                }
            }
        }

        content()
    }
}
