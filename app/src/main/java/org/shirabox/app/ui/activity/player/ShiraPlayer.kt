package org.shirabox.app.ui.activity.player

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.FileDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.flow.firstOrNull
import org.shirabox.app.ui.activity.player.presentation.PlayerScaffold
import org.shirabox.app.ui.activity.player.presentation.SettingsBottomSheet
import org.shirabox.core.datastore.DataStoreScheme
import org.shirabox.core.entity.EpisodeEntity
import org.shirabox.core.model.Quality
import org.shirabox.core.model.StreamProtocol
import org.shirabox.data.content.AbstractContentRepository


@OptIn(UnstableApi::class)
@Composable
fun ShiraPlayer(exoPlayer: ExoPlayer, model: PlayerViewModel) {
    val interactionSource = remember(::MutableInteractionSource)

    val playlist by model.playlistFlow().collectAsStateWithLifecycle(initialValue = emptyList())

    Column {
        Box(
            modifier = Modifier
                .background(Color(0xFF000000))
                .fillMaxSize()
                .clickable(
                    interactionSource = interactionSource, indication = null
                ) {
                    model.controlsVisibilityState = !model.controlsVisibilityState
                    model.hideUi()
                }
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = playlist.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                PlayerSurface(
                    exoPlayer = exoPlayer,
                    model = model,
                    playlist = playlist
                )
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun PlayerSurface(
    exoPlayer: ExoPlayer,
    model: PlayerViewModel,
    playlist: List<EpisodeEntity>
) {
    val context = LocalContext.current
    val playerView = PlayerView(context)
    val coroutineScope = rememberCoroutineScope()

    val defaultQualityState = model.defaultQualityPreferenceFlow(context)
        .collectAsState(initial = DataStoreScheme.FIELD_DEFAULT_QUALITY.defaultValue)

    LaunchedEffect(Unit) {
        model.fetchEpisodePositions()
    }

    LaunchedEffect(defaultQualityState.value) {
        model.currentQuality = Quality.valueOfInt(
            defaultQualityState.value ?: DataStoreScheme.FIELD_DEFAULT_QUALITY.defaultValue
        )
    }

    LaunchedEffect(model.episodesPositions[model.initialEpisode]) {
        val startPosition = model.episodesPositions[model.initialEpisode]

        if (!model.coldStartSeekApplied) startPosition?.let { startPos ->
            val quality = model.defaultQualityPreferenceFlow(context).firstOrNull()
                ?: DataStoreScheme.FIELD_DEFAULT_QUALITY.defaultValue
            val repository = model.currentRepository!!

            exoPlayer.apply {
                setMediaSources(playlist.map { episodeEntity ->
                    val stream = episodeEntity.videos.entries.findLast {
                        it.key == Quality.valueOfInt(quality)
                    } ?: episodeEntity.videos.maxBy { it.key.quality }

                    val offlineFilePath = episodeEntity.offlineVideos?.entries?.findLast {
                        it.key == Quality.valueOfInt(quality)
                    } ?: episodeEntity.offlineVideos?.maxByOrNull { it.key.quality }

                    val offlineFileUri = offlineFilePath?.let {
                        Uri.parse(it.value)
                    }

                    val offlineMediaSource = offlineFileUri?.let { uri ->
                        ProgressiveMediaSource
                            .Factory(FileDataSource.Factory())
                            .createMediaSource(MediaItem.fromUri(uri))
                    }

                    val streamMediaSource = when (repository.streamingType) {
                        StreamProtocol.MPEG -> ProgressiveMediaSource
                            .Factory(DefaultHttpDataSource.Factory().setDefaultRequestProperties(repository.hostHeaders()))
                        StreamProtocol.HLS -> HlsMediaSource
                            .Factory(DefaultHttpDataSource.Factory().setDefaultRequestProperties(repository.hostHeaders()))
                    }.createMediaSource(MediaItem.fromUri(stream.value))

                    return@map offlineMediaSource ?: streamMediaSource
                })

                val seekIndex = playlist.indexOfFirst { it.episode == model.initialEpisode }

                exoPlayer.seekTo(
                    seekIndex,
                    startPos
                )

                playWhenReady = true
            }

            model.coldStartSeekApplied = true
        }
    }

    LaunchedEffect(playlist) {
        if (playlist.isNotEmpty()) model.seekNewEpisodes(playlist)
    }

    LaunchedEffect(model.currentQuality, playlist.size) {
        if (exoPlayer.mediaItemCount != 0 && model.coldStartSeekApplied) {
            rebuildExoPlaylist(
                exoPlayer,
                playlist,
                model.currentRepository!!,
                model.currentQuality
            )
        }
    }

    LaunchedEffect(model.playbackSpeed) { exoPlayer.setPlaybackSpeed(model.playbackSpeed) }

    DisposableEffect(key1 = Unit) {
        exoPlayer.addListener(PlayerStateListener(coroutineScope, model))
        exoPlayer.pauseAtEndOfMediaItems = true

        onDispose {
            val currentEpisode = playlist.getOrNull(exoPlayer.currentMediaItemIndex)?.episode

            saveEpisodeState(currentEpisode, exoPlayer, model)
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = {
            playerView.apply {
                player = exoPlayer
                layoutParams =
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                keepScreenOn = true
                useController = false
            }
        }
    )

    PlayerScaffold(exoPlayer = exoPlayer, playlist = playlist, model = model)

    SettingsBottomSheet(exoPlayer = exoPlayer, playlist = playlist, model = model)
}

@OptIn(UnstableApi::class)
private fun rebuildExoPlaylist(
    exoPlayer: ExoPlayer,
    playlist: List<EpisodeEntity>,
    repository: AbstractContentRepository,
    currentQuality: Quality
) {
    val currentPosition = exoPlayer.currentPosition
    val currentItemIndex = exoPlayer.currentMediaItemIndex

    exoPlayer.apply {
        setMediaSources(
            playlist.map {
                val streamMediaSource = when (repository.streamingType) {
                    StreamProtocol.MPEG -> ProgressiveMediaSource
                        .Factory(DefaultHttpDataSource.Factory().setDefaultRequestProperties(repository.hostHeaders()))
                    StreamProtocol.HLS -> HlsMediaSource
                        .Factory(DefaultHttpDataSource.Factory().setDefaultRequestProperties(repository.hostHeaders()))
                }.createMediaSource(MediaItem.fromUri(it.videos[currentQuality] ?: ""))
                streamMediaSource
            }
        )
        seekTo(currentItemIndex, currentPosition)
    }
}

private fun saveEpisodeState(currentEpisode: Int?, exoPlayer: ExoPlayer, model: PlayerViewModel) {
    currentEpisode?.let {
        if (exoPlayer.playbackState == ExoPlayer.STATE_READY || exoPlayer.playbackState == ExoPlayer.STATE_ENDED) {
            model.saveEpisodePosition(currentEpisode, exoPlayer.currentPosition, exoPlayer.duration)
        }
    }
}