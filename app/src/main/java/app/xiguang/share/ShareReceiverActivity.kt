package app.xiguang.share

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import app.xiguang.XiguangApplication
import app.xiguang.data.repository.SaveResult
import app.xiguang.domain.model.FolderMutationResult
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
            var folderCreationError by remember { mutableStateOf<FolderMutationResult?>(null) }
            XiguangTheme {
                ShareSaveScreen(
                    payload = payload,
                    folders = folders,
                    folderCreationError = folderCreationError,
                    onCancel = ::finish,
                    onCreateFolder = { name ->
                        lifecycleScope.launch {
                            folderCreationError = repository.createFolder(name, parentId = null)
                                .takeUnless { it == FolderMutationResult.Success }
                        }
                    },
                    onDismissFolderCreationError = { folderCreationError = null },
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
