package org.shirabox.app.ui.screen.explore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.shirabox.app.R
import org.shirabox.app.ui.component.general.DespondencyEmoticon
import org.shirabox.app.ui.component.general.ScaredEmoticon
import java.io.IOException

@Composable
fun TroubleMessage(
    model: ExploreViewModel = hiltViewModel(),
    observationStatus: ExploreViewModel.ObservationStatus
) {
    AnimatedVisibility(
        visible = observationStatus.status == ExploreViewModel.Status.Failure,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (observationStatus.exception) {
                    is IOException -> DespondencyEmoticon(text = stringResource(id = R.string.no_internet_connection_variant))
                    else -> ScaredEmoticon(text = stringResource(id = R.string.no_contents))
                }

                OutlinedButton(
                    shape = RoundedCornerShape(32),
                    onClick = { model.refresh(false) }
                ) {
                    Text(stringResource(id = R.string.refresh))
                }
            }
        }
    }
}