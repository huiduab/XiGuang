package app.xiguang.domain.parser

import android.content.Intent
import android.net.Uri
import androidx.core.content.IntentCompat
import app.xiguang.domain.model.ContentType
import app.xiguang.domain.model.SharedPayload

object ShareIntentParser {
    fun parse(intent: Intent, sharedFromPackage: String? = null): SharedPayload {
        val mimeType = intent.type
        val sharedText = intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()
        val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT)
            ?: intent.getStringExtra(Intent.EXTRA_TITLE)
        val originalUrl = UrlExtractor.firstUrl(sharedText)
        val canonicalUrl = UrlNormalizer.normalize(originalUrl)
        val stream = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
        val contentType = when {
            mimeType?.startsWith("image/") == true -> ContentType.IMAGE
            mimeType == "application/pdf" -> ContentType.DOCUMENT
            originalUrl != null -> ContentType.LINK
            else -> ContentType.TEXT
        }
        val title = subject
            ?.trim()
            ?.takeIf(String::isNotEmpty)
            ?: sharedText
                ?.replace(originalUrl.orEmpty(), "")
                ?.trim()
                ?.lineSequence()
                ?.firstOrNull()
                ?.take(80)
                ?.takeIf(String::isNotEmpty)
            ?: when (contentType) {
                ContentType.IMAGE -> "图片收藏"
                ContentType.DOCUMENT -> "文档收藏"
                ContentType.LINK -> "网页收藏"
                ContentType.TEXT -> "文字收藏"
            }

        return SharedPayload(
            title = title,
            sharedText = sharedText,
            originalUrl = originalUrl,
            canonicalUrl = canonicalUrl,
            platform = PlatformDetector.detect(canonicalUrl ?: originalUrl),
            contentType = contentType,
            mimeType = mimeType,
            previewUri = stream?.toString(),
            sharedFromPackage = sharedFromPackage,
        )
    }
}
