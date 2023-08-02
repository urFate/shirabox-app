package com.shirabox.shirabox.ui.component.general

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shirabox.shirabox.R

@Composable
fun RatingView(averageRating: Double, votes: Int, values: Map<Int, Float>){
    Row(
        modifier = Modifier.padding(32.dp, 0.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = averageRating.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp
            )
            Text(
                text = stringResource(id = R.string.votes, votes),
                fontWeight = FontWeight.Thin,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            values.forEach { (label, value) ->
                RatingBar(label = label, value = value)
            }
        }
    }
}

@Composable
private fun RatingBar(label: Int, value: Float) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.toString(),
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.secondary
        )

        if(label <= 9) {
            Spacer(modifier = Modifier.width(3.dp))
        }

        LinearProgressIndicator(
            modifier = Modifier
                .clip(RoundedCornerShape(100))
                .height(9.dp)
                .fillMaxWidth(),
            progress = value,
            color = MaterialTheme.colorScheme.tertiary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}