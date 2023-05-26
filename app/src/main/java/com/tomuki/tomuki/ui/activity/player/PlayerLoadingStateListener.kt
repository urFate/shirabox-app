package com.tomuki.tomuki.ui.activity.player

import androidx.compose.runtime.MutableState
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerLoadingStateListener(
    private val coroutineScope: CoroutineScope,
    private val player: ExoPlayer,
    private val controlsVisibilityState: MutableState<Boolean>
) : Player.Listener {

    override fun onIsLoadingChanged(isLoading: Boolean) {
        if(!isLoading){
            coroutineScope.launch {
                delay(2000).let{
                    controlsVisibilityState.value = false
                }
            }
            player.removeListener(this)
        }
    }
}