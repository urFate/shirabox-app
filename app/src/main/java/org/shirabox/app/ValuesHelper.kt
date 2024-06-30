package org.shirabox.app

import android.content.Context
import org.shirabox.core.model.ContentKind
import org.shirabox.core.model.ReleaseStatus

object ValuesHelper {
    fun decodeKind(contentKind: ContentKind, context: Context): String = when (contentKind) {
        ContentKind.TV -> context.getString(R.string.kind_tv)
        ContentKind.MOVIE -> context.getString(R.string.kind_movie)
        ContentKind.OVA -> context.getString(R.string.kind_ova)
        ContentKind.ONA -> context.getString(R.string.kind_ona)
        ContentKind.SPECIAL -> context.getString(R.string.kind_special)
        ContentKind.OTHER -> context.getString(R.string.kind_other)
    }

    fun decodeStatus(releaseStatus: ReleaseStatus, context: Context): String =
        when (releaseStatus) {
            ReleaseStatus.FINISHED -> context.getString(R.string.release_status_finished)
            ReleaseStatus.RELEASING -> context.getString(R.string.release_status_releasing)
            ReleaseStatus.ANNOUNCED -> context.getString(R.string.release_status_announced)
            ReleaseStatus.PAUSED -> context.getString(R.string.release_status_paused)
            ReleaseStatus.DISCOUNTED -> context.getString(R.string.release_status_discounted)
            ReleaseStatus.UNKNOWN -> context.getString(R.string.release_status_unknown)
        }

}