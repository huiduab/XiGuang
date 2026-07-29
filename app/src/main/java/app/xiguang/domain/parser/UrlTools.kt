package app.xiguang.domain.parser

import app.xiguang.domain.model.Platform
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale

object UrlExtractor {
    private val urlPattern = Regex("""https?://[^\s<>"'，。；！？）】]+""")

    fun firstUrl(text: String?): String? =
        text?.let(urlPattern::find)?.value?.trimEnd('.', ',', ';', ')', ']', '}')
}

object UrlNormalizer {
    private val ignoredQueryKeys = setOf(
        "fbclid",
        "gclid",
        "igshid",
        "mc_cid",
        "mc_eid",
        "share_source",
        "spm",
    )

    fun normalize(rawUrl: String?): String? {
        if (rawUrl.isNullOrBlank()) return null
        return runCatching {
            val uri = URI(rawUrl.trim())
            val scheme = uri.scheme?.lowercase(Locale.ROOT)
            if (scheme != "http" && scheme != "https") return null

            val host = uri.host?.lowercase(Locale.ROOT) ?: return null
            val normalizedPort = when {
                scheme == "http" && uri.port == 80 -> -1
                scheme == "https" && uri.port == 443 -> -1
                else -> uri.port
            }
            val normalizedPath = when {
                uri.path.isNullOrBlank() -> ""
                uri.path == "/" -> ""
                else -> uri.path.trimEnd('/')
            }
            val normalizedQuery = normalizeQuery(uri.rawQuery)
            URI(
                scheme,
                uri.userInfo,
                host,
                normalizedPort,
                normalizedPath,
                normalizedQuery,
                null,
            ).toASCIIString()
        }.getOrNull()
    }

    private fun normalizeQuery(rawQuery: String?): String? {
        if (rawQuery.isNullOrBlank()) return null
        val charset = StandardCharsets.UTF_8
        return rawQuery
            .split('&')
            .mapNotNull { part ->
                val pieces = part.split('=', limit = 2)
                val key = URLDecoder.decode(pieces[0], charset)
                val lowerKey = key.lowercase(Locale.ROOT)
                if (lowerKey.startsWith("utm_") || lowerKey in ignoredQueryKeys) {
                    null
                } else {
                    val value = pieces.getOrNull(1)?.let { URLDecoder.decode(it, charset) }
                    URLEncoder.encode(key, charset) to value?.let { URLEncoder.encode(it, charset) }
                }
            }
            .sortedBy { it.first }
            .joinToString("&") { (key, value) ->
                if (value == null) key else "$key=$value"
            }
            .ifBlank { null }
    }
}

object PlatformDetector {
    fun detect(url: String?): Platform {
        val host = runCatching {
            URI(url.orEmpty()).host?.lowercase(Locale.ROOT)
        }.getOrNull() ?: return Platform.OTHER

        return when {
            host.matchesDomain("x.com") || host.matchesDomain("twitter.com") -> Platform.X
            host.matchesDomain("weibo.com") || host.matchesDomain("weibo.cn") -> Platform.WEIBO
            host.matchesDomain("xiaohongshu.com") || host.matchesDomain("xhslink.com") ->
                Platform.XIAOHONGSHU
            host.matchesDomain("douyin.com") || host.matchesDomain("iesdouyin.com") ->
                Platform.DOUYIN
            host.matchesDomain("bilibili.com") || host.matchesDomain("b23.tv") ->
                Platform.BILIBILI
            host.matchesDomain("zhihu.com") -> Platform.ZHIHU
            host.matchesDomain("youtube.com") || host.matchesDomain("youtu.be") ->
                Platform.YOUTUBE
            else -> Platform.BLOG
        }
    }

    private fun String.matchesDomain(domain: String): Boolean =
        this == domain || endsWith(".$domain")
}
