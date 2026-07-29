package app.xiguang.share

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import app.xiguang.XiguangApplication
import app.xiguang.data.repository.SaveResult
import app.xiguang.domain.parser.ShareIntentParser
import app.xiguang.ui.theme.XiguangTheme
import kotlinx.coroutines.launch

class ShareReceiverActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (intent.action != Intent.ACTION_SEND && intent.action != Intent.ACTION_SEND_MULTIPLE) {
            finish()
            return
        }

        val application = application as XiguangApplication
        val repository = application.collectionRepository
        val payload = ShareIntentParser.parse(
            intent = intent,
            sharedFromPackage = callingPackage ?: referrer?.host,
        )

        setContent {
            val folders = repository.folders.collectAsStateWithLifecycle(emptyList()).value
            XiguangTheme {
                ShareSaveScreen(
                    payload = payload,
                    folders = folders,
                    onCancel = ::finish,
                    onSave = { folderId ->
                        lifecycleScope.launch {
                            val result = repository.save(payload, folderId)
                            setResult(
                                if (result is SaveResult.Created) RESULT_OK else RESULT_FIRST_USER,
                            )
                            finish()
                        }
                    },
                )
            }
        }
    }
}
