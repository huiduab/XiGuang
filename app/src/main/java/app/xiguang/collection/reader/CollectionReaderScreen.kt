package app.xiguang.collection.reader

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import app.xiguang.R
import app.xiguang.collection.detail.CollectionContentLauncher
import app.xiguang.collection.detail.CollectionDetailViewModel

@Composable
fun CollectionReaderRoute(
    onBack: () -> Unit,
    viewModel: CollectionDetailViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val collection = state.collection
    CollectionReaderScreen(
        url = collection?.originalUrl,
        onBack = onBack,
        onOpenExternal = {
            if (collection?.originalUrl?.let { url -> CollectionContentLauncher.openExternalUrl(context, url) } != true) {
                android.widget.Toast.makeText(context, R.string.content_open_error, android.widget.Toast.LENGTH_SHORT).show()
            }
        },
    )
}

@Composable
private fun CollectionReaderScreen(
    url: String?,
    onBack: () -> Unit,
    onOpenExternal: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, stringResource(R.string.navigate_back))
            }
            Text(
                text = stringResource(R.string.reader_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onOpenExternal) {
                Icon(Icons.Outlined.OpenInNew, stringResource(R.string.open_external))
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        if (url.isWebUrl()) {
            WebContent(url)
        } else {
            Text(
                text = stringResource(R.string.reader_invalid_url),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(28.dp),
            )
        }
    }
}

private fun String?.isWebUrl(): Boolean {
    val scheme = this?.let(android.net.Uri::parse)?.scheme?.lowercase()
    return scheme == "http" || scheme == "https"
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun WebContent(url: String) {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = false
            webViewClient = WebViewClient()
        }
    }
    DisposableEffect(webView) {
        onDispose { webView.destroy() }
    }
    AndroidView(
        factory = { webView },
        modifier = Modifier.fillMaxSize(),
        update = { view -> if (view.url != url) view.loadUrl(url) },
    )
}
