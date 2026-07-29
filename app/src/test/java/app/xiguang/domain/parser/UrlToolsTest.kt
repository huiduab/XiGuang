package app.xiguang.domain.parser

import app.xiguang.domain.model.Platform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UrlToolsTest {
    @Test
    fun normalizer_removesTrackingAndFragment() {
        assertEquals(
            "https://x.com/example/status/123?a=1",
            UrlNormalizer.normalize(
                "https://X.com/example/status/123/?utm_source=test&a=1#detail",
            ),
        )
    }

    @Test
    fun detector_usesContentDomain() {
        assertEquals(
            Platform.XIAOHONGSHU,
            PlatformDetector.detect("https://www.xiaohongshu.com/explore/123"),
        )
        assertEquals(
            Platform.DOUYIN,
            PlatformDetector.detect("https://v.douyin.com/abc"),
        )
        assertEquals(
            Platform.BLOG,
            PlatformDetector.detect("https://example.com/article"),
        )
    }

    @Test
    fun extractor_returnsFirstHttpUrl() {
        assertEquals(
            "https://example.com/post",
            UrlExtractor.firstUrl("推荐阅读 https://example.com/post 真的不错"),
        )
        assertNull(UrlExtractor.firstUrl("没有网址"))
    }
}
