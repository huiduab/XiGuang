package app.xiguang.data.file

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

interface AttachmentStore {
    suspend fun persist(uriText: String?, mimeType: String?): String?
}

class LocalAttachmentStore(
    private val context: Context,
) : AttachmentStore {
    override suspend fun persist(uriText: String?, mimeType: String?): String? = withContext(Dispatchers.IO) {
        if (uriText == null) return@withContext null

        val source = Uri.parse(uriText)
        if (source.scheme != "content") return@withContext uriText

        try {
            val directory = File(context.filesDir, "shared-content")
            if (!directory.exists() && !directory.mkdirs()) return@withContext uriText

            val target = File(directory, "${UUID.randomUUID()}${extensionFor(mimeType)}")
            context.contentResolver.openInputStream(source)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: return@withContext uriText

            Uri.fromFile(target).toString()
        } catch (_: IOException) {
            uriText
        } catch (_: SecurityException) {
            uriText
        }
    }

    private fun extensionFor(mimeType: String?): String = when (mimeType?.lowercase()) {
        "image/jpeg", "image/jpg" -> ".jpg"
        "image/png" -> ".png"
        "image/webp" -> ".webp"
        "application/pdf" -> ".pdf"
        else -> ".bin"
    }
}
