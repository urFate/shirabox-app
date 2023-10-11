package live.shirabox.shirabox.ui.activity.player

import androidx.media3.common.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import live.shirabox.core.util.Values

class PlayerLoadingStateListener(
    private val coroutineScope: CoroutineScope,
    private val model: PlayerViewModel
) : Player.Listener {

    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState != Player.STATE_BUFFERING) {
            coroutineScope.launch {
                delay(Values.CONTROLS_HIDE_DELAY).let {
                    model.controlsVisibilityState = false
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