package org.shirabox.core.media

import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import java.io.File

object MpegTools {
    fun repairMpeg(path: String, onFinish: (Boolean) -> Unit) {
        val tempFilePath = path.replace(".mp4", ".fixed.mp4")
        val session = FFmpegKit.execute("-i $path -c copy $tempFilePath")

        File(path).delete()
        File(tempFilePath).renameTo(File(path))

        onFinish(ReturnCode.isSuccess(session.returnCode))
    }
}