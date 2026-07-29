package app.xiguang.collection.reader

import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CollectionReaderWebViewTest {
    @Test
    fun readingConfiguration_supportsModernWebsitesWithoutFileAccess() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val webView = WebView(instrumentation.targetContext)
            try {
                webView.configureForReading()

                assertTrue(webView.settings.javaScriptEnabled)
                assertTrue(webView.settings.domStorageEnabled)
                assertFalse(webView.settings.allowFileAccess)
                assertFalse(webView.settings.allowContentAccess)
                assertTrue(CookieManager.getInstance().acceptCookie())
                assertTrue(CookieManager.getInstance().acceptThirdPartyCookies(webView))
                assertTrue(webView.settings.mixedContentMode == WebSettings.MIXED_CONTENT_NEVER_ALLOW)
            } finally {
                webView.destroy()
            }
        }
    }
}
