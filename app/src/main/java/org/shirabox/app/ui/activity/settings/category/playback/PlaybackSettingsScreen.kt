package org.shirabox.app.ui.activity.settings.category.playback

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.shirabox.app.R
import org.shirabox.app.ui.activity.auth.AuthActivity
import org.shirabox.app.ui.activity.settings.CombinedSwitchPreference
import org.shirabox.app.ui.activity.settings.OptionsBlock
import org.shirabox.app.ui.activity.settings.Preference
import org.shirabox.app.ui.activity.settings.SwitchPreference
import org.shirabox.app.ui.component.general.QualityDialog
import org.shirabox.core.datastore.AppDataStore
import org.shirabox.core.datastore.DataStoreScheme
import org.shirabox.core.model.AuthService
import org.shirabox.core.model.Quality
import org.shirabox.data.animeskip.AnimeSkipRepository
import java.io.IOException

@Composable
fun PlaybackSettingsScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val qualityVisibilityState = remember { mutableStateOf(false) }
    val animeskipKeyVisibilityState = remember { mutableStateOf(false) }
    val instantSeekVisibilityState = remember { mutableStateOf(false) }

    val currentQualityFlowState =
        AppDataStore.read(context, DataStoreScheme.FIELD_DEFAULT_QUALITY.key).collectAsState(
            initial = DataStoreScheme.FIELD_DEFAULT_QUALITY.defaultValue
        )

    val animeSkipClientKeyFlowState =
        AppDataStore.read(context, DataStoreScheme.FIELD_ANIMESKIP_USER_CLIENT_ID).collectAsState(
            initial = null
        )
    val animeSkipKeyValidityState: MutableState<Boolean?> = remember { mutableStateOf(null) }

    LaunchedEffect(animeSkipClientKeyFlowState.value) {
        animeSkipClientKeyFlowState.value?.let { clientKey ->
            AnimeSkipRepository.checkClientKeyValidity(clientKey).catch {
                it.printStackTrace()

                /**
                 * Emit true value on network inability exceptions (when seems device is offline)
                 */

                when(it) {
                    is IOException -> emit(true)
                    else -> emit(false)
                }
            }.collect {
                animeSkipKeyValidityState.value = it
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Preference(
            headlineContent = { Text(stringResource(id = R.string.playback_default_quality)) },
            supportingContent = {
                Text(
                    text = stringResource(id = R.string.playback_default_quality_desc),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            },
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.badge_fhd),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "quality"
                )
            }
        ) {
            qualityVisibilityState.value = true
        }

        OptionsBlock(title = stringResource(id = R.string.opening_preferences)) {
            val isError = animeSkipKeyValidityState.value?.let { !it } ?: false

            SwitchPreference(
                headlineContent = { Text(stringResource(id = R.string.opening_skip_preference)) },
                supportingContent = {
                    Text(
                        text = stringResource(id = R.string.opening_skip_preference_desc),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                },
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.player_skip_forward),
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "animeskip"
                    )
                },
                dsField = DataStoreScheme.FIELD_OPENING_SKIP
            )

            when(animeSkipClientKeyFlowState.value) {
                null, "" -> {
                    Preference(
                        headlineContent = { Text(stringResource(id = R.string.animeskip_preference)) },
                        supportingContent = {
                            Text(
                                text = stringResource(id = R.string.animeskip_preference_desc),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        },
                        leadingContent = {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(id = R.drawable.animeskip),
                                tint = MaterialTheme.colorScheme.primary,
                                contentDescription = "animeskip"
                            )
                        }
                    ) {
                        context.startActivity(
                            Intent(
                                context,
                                AuthActivity::class.java
                            ).apply { putExtra("auth_service", AuthService.AnimeSkip.name) })
                    }
                }
                else -> {
                    CombinedSwitchPreference(
                        headlineContent = { Text(stringResource(id = R.string.animeskip_preference)) },
                        supportingContent = {
                            Text(
                                text = when (animeSkipKeyValidityState.value) {
                                    true, null -> stringResource(id = R.string.animeskip_preference_desc_authorized)
                                    false -> stringResource(id = R.string.animeskip_invalid_key)
                                },
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = when (isError) {
                                    true -> MaterialTheme.colorScheme.error
                                    false -> Color.Unspecified
                                }
                            )
                        },
                        leadingContent = {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(id = R.drawable.animeskip),
                                tint = MaterialTheme.colorScheme.primary,
                                contentDescription = "animeskip"
                            )
                        },
                        switchEnabled = !isError,
                        uncheckSwitch = isError,
                        dsField = DataStoreScheme.FIELD_USE_ANIMESKIP
                    ) {
                        animeskipKeyVisibilityState.value = true
                    }
                }
            }
        }

        OptionsBlock(title = stringResource(id = R.string.interaction_options_block)) {
            Preference(
                headlineContent = { Text(stringResource(id = R.string.instant_seek_time)) },
                supportingContent = {
                    Text(
                        text = stringResource(id = R.string.instant_seek_time_desc),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                },
                leadingContent = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.forward),
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "instant seek"
                    )
                }
            ) {
                instantSeekVisibilityState.value = true
            }
        }
    }

    QualityDialog(
        title = stringResource(id = R.string.playback_default_quality),
        description = stringResource(id = R.string.playback_default_quality_desc),
        icon = ImageVector.vectorResource(R.drawable.badge_fhd),
        visibilityState = qualityVisibilityState,
        maxQuality = Quality.FHD,
        autoSelect = Quality.valueOfInt(currentQualityFlowState.value ?: 1080)
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            AppDataStore.write(context, DataStoreScheme.FIELD_DEFAULT_QUALITY.key, it.quality)
        }
    }
    AnimeSkipDialog(animeskipKeyVisibilityState)
    InstantSeekDialog(instantSeekVisibilityState)
}