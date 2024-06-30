package live.shirabox.core.util

import android.content.Intent
import live.shirabox.core.entity.EpisodeEntity
import live.shirabox.core.model.Content

object IntentExtras {
    fun playerIntentExtras(
        content: Content,
        episodeEntity: EpisodeEntity,
        team: String
    ): Intent {
        return Intent().apply {
            putExtra("content_uid", episodeEntity.contentUid)
            putExtra("name", content.name)
            putExtra("en_name", content.enName)
            putExtra("acting_team", team)
            putExtra("repository", episodeEntity.source)
            putExtra("episode", episodeEntity.episode)
        }
    }
}