package org.shirabox.app.ui.activity.resource

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.shirabox.app.R
import org.shirabox.app.ui.component.general.SpecialSnackBar
import org.shirabox.app.ui.theme.errorContainerLight
import org.shirabox.app.ui.theme.onErrorContainerLight

@Composable
fun ResourceSnackbarHost(
    model: ResourceViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState
) {
    val favouritesMessage =
        stringResource(id = if (!model.isFavourite.value) R.string.favourites_removal_message else R.string.favourites_added_message)
    var coldStart by remember { mutableStateOf(true) }

    LaunchedEffect(model.isFavourite.value) {
        if (coldStart) {
            coldStart = false
        } else {
            snackbarHostState.currentSnackbarData?.let(SnackbarData::dismiss)
            snackbarHostState.showSnackbar(message = favouritesMessage, duration = SnackbarDuration.Short)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp, 40.dp),
        contentAlignment = Alignment.BottomStart,
    ) {
        SnackbarHost(
            hostState = snackbarHostState
        ) { snackbarData: SnackbarData ->
            SpecialSnackBar(
                icon = {
                    Icon(
                        imageVector = if (model.isFavourite.value) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                        contentDescription = "star"
                    )
                },
                message = snackbarData.visuals.message,
                containerColor = errorContainerLight,
                contentColor = onErrorContainerLight,
                clickable = false,
                closeable = false,
                onCloseClick = snackbarData::dismiss
            )
        }
    }
}