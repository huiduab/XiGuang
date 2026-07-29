package app.xiguang.collection.detail

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import app.xiguang.domain.model.SavedCollection
import java.io.File

internal object CollectionContentLauncher {
    fun openExternalUrl(context: Context, url: String): Boolean {
        val uri = Uri.parse(url)
        if (uri.scheme?.lowercase() !in setOf("http", "https")) return false
        return launch(context, Intent(Intent.ACTION_VIEW, uri))
    }

    fun openAttachment(context: Context, collection: SavedCollection): Boolean {
        val uriText = collection.previewUri ?: return false
        return try {
            val rawUri = Uri.parse(uriText)
            val uri = if (rawUri.scheme == "file") {
                val file = File(rawUri.path ?: return false)
                if (!file.isFile) return false
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } else {
                rawUri
            }
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, collection.mimeType ?: "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            launch(context, intent)
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    private fun launch(context: Context, intent: Intent): Boolean = try {
        context.startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    } catch (_: IllegalArgumentException) {
        false
    }
}
