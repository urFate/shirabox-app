package live.shirabox.core.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL

object UpdateManager {

    private var downloadInterruptRequested = false
    private var downloadInProcess = false

    suspend fun downloadFile(
        url: URL,
        file: File,
        onProgress: (progress: Float) -> Unit,
        onFinish: (exception: Exception?) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val jobDeferred = async(Dispatchers.IO) {
                try {
                    downloadInProcess = true
                    val length = url.openConnection().apply {
                        connect()
                    }.contentLength

                    val input = BufferedInputStream(url.openStream(), 8192)
                    val output = FileOutputStream(file)
                    val bytes = ByteArray(1024)
                    var count = input.read(bytes)
                    var total = 0L

                    while (count != -1) {
                        if (downloadInterruptRequested) {
                            input.close()
                            if (file.exists()) file.delete()
                            break
                        }

                        total += count
                        output.write(bytes, 0, count)

                        onProgress(total.toFloat() / length)
                        count = input.read(bytes)
                    }

                    output.flush()
                    output.close()
                    input.close()

                    downloadInterruptRequested = false
                    downloadInProcess = false

                    return@async null
                } catch (ex: Exception) {
                    return@async ex
                }
            }

            onFinish(jobDeferred.await())
        }
    }

    fun installPackage(context: Context, applicationId: String, file: File) {
        if (file.exists()) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(
                uriFromFile(context, applicationId, file),
                "application/vnd.android.package-archive"
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                context.startActivity(intent)
            } catch (ex: Exception) {
                throw ex
            }
        }
    }

    private fun uriFromFile(context: Context, applicationId: String, file: File): Uri {
        return FileProvider.getUriForFile(context, "$applicationId.provider", file);
    }

    fun cancelDownload() {
        if (downloadInProcess) downloadInterruptRequested = true
    }
}