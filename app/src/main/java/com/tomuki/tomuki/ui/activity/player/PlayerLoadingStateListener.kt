package com.tomuki.tomuki.ui.activity.player

import androidx.compose.runtime.MutableState
import androidx.media3.common.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerLoadingStateListener(
    private val coroutineScope: CoroutineScope,
    private val controlsVisibilityState: MutableState<Boolean>
) : Player.Listener {

    override fun onIsLoadingChanged(isLoading: Boolean) {
        if(!isLoading){
            coroutineScope.launch {
                delay(1000).let{
                    controlsVisibilityState.value = false
                }
            }
        }
    }

    override fun onEvents(player: Player, events: Player.Events) {
        if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)) {
            player.playWhenReady = true
        }
    }
}