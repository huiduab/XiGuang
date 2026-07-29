package app.xiguang.collection.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.xiguang.R
import app.xiguang.domain.model.ContentType
import app.xiguang.domain.model.SavedCollection
import app.xiguang.domain.model.DefaultOpenMode
import java.text.DateFormat
import java.util.Date

@Composable
fun CollectionDetailRoute(
    onBack: () -> Unit,
    onReadInApp: (Long) -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: CollectionDetailViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    CollectionDetailScreen(
        state = state,
        onBack = onBack,
        onReadInApp = { viewModel.markReadAndOpen(onReadInApp) },
        onOpenPrimary = { collection ->
            if (state.defaultOpenMode == DefaultOpenMode.IN_APP) viewModel.markReadAndOpen(onReadInApp)
            else CollectionContentLauncher.openExternalUrl(context, collection.originalUrl.orEmpty())
        },
        onEdit = { collection -> onEdit(collection.id) },
        onDelete = { viewModel.delete(onBack) },
        onOpenExternal = { collection ->
            val opened = when (collection.contentType) {
                ContentType.LINK -> collection.originalUrl?.let { CollectionContentLauncher.openExternalUrl(context, it) } ?: false
                ContentType.IMAGE,
                ContentType.DOCUMENT,
                -> CollectionContentLauncher.openAttachment(context, collection)

                ContentType.TEXT -> false
            }
            if (!opened) android.widget.Toast.makeText(context, R.string.content_open_error, android.widget.Toast.LENGTH_SHORT).show()
        },
    )
}

@Composable
internal fun CollectionDetailScreen(
    state: CollectionDetailUiState,
    onBack: () -> Unit,
    onReadInApp: (Long) -> Unit,
    onOpenPrimary: (SavedCollection) -> Unit,
    onEdit: (SavedCollection) -> Unit,
    onDelete: () -> Unit,
    onOpenExternal: (SavedCollection) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        var showDeleteDialog by remember { mutableStateOf(false) }
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
                text = stringResource(R.string.collection_detail_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            state.collection?.let { collection ->
                TextButton(onClick = { onEdit(collection) }) { Text(stringResource(R.string.edit_action)) }
                TextButton(onClick = { showDeleteDialog = true }) { Text(stringResource(R.string.delete_action)) }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        when (val collection = state.collection) {
            null -> DetailMissing(onBack)
            else -> DetailContent(
                collection = collection,
                onOpenPrimary = onOpenPrimary,
                defaultOpenMode = state.defaultOpenMode,
                onOpenExternal = onOpenExternal,
            )
        }
        if (showDeleteDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(stringResource(R.string.delete_collections_title)) },
                text = { Text(stringResource(R.string.delete_single_message)) },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }) { Text(stringResource(R.string.delete_action)) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.cancel_action)) }
                },
            )
        }
    }
}

@Composable
private fun DetailMissing(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(96.dp))
        Text(
            text = stringResource(R.string.collection_detail_missing),
            style = MaterialTheme.typography.headlineSmall,
        )
        TextButton(onClick = onBack) { Text(stringResource(R.string.navigate_back)) }
    }
}

@Composable
private fun DetailContent(
    collection: SavedCollection,
    onOpenPrimary: (SavedCollection) -> Unit,
    defaultOpenMode: DefaultOpenMode,
    onOpenExternal: (SavedCollection) -> Unit,
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
        Text(
            text = collection.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
        collection.sharedText?.takeIf(String::isNotBlank)?.let { sharedText ->
            Text(
                text = sharedText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 14.dp),
            )
        }
        collection.originalUrl?.let { url ->
            Text(
                text = url,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 14.dp),
            )
        }

        when (collection.contentType) {
            ContentType.LINK -> collection.originalUrl?.let {
                Button(
                    onClick = { onOpenPrimary(collection) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                ) { Text(stringResource(if (defaultOpenMode == DefaultOpenMode.IN_APP) R.string.read_in_app else R.string.open_external)) }
                OutlinedButton(
                    onClick = { onOpenExternal(collection) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                ) {
                    Icon(Icons.Outlined.OpenInNew, contentDescription = null)
                    Text(stringResource(R.string.open_external), modifier = Modifier.padding(start = 8.dp))
                }
            }

            ContentType.IMAGE,
            ContentType.DOCUMENT,
            -> OutlinedButton(
                onClick = { onOpenExternal(collection) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
            ) {
                Icon(Icons.Outlined.OpenInNew, contentDescription = null)
                Text(stringResource(R.string.open_with_system), modifier = Modifier.padding(start = 8.dp))
            }

            ContentType.TEXT -> Unit
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 28.dp),
            color = MaterialTheme.colorScheme.outline,
        )
        DetailField(stringResource(R.string.detail_platform), collection.platform.label)
        DetailField(stringResource(R.string.detail_content_type), detailContentTypeLabel(collection.contentType))
        DetailField(
            stringResource(R.string.detail_read_state),
            if (collection.isRead) stringResource(R.string.read_state_read) else stringResource(R.string.read_state_unread),
        )
        collection.folderPath?.let { DetailField(stringResource(R.string.detail_folder), it) }
        collection.note?.takeIf(String::isNotBlank)?.let { DetailField(stringResource(R.string.detail_note), it) }
        DetailField(
            stringResource(R.string.detail_saved_at),
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(collection.createdAt)),
        )
    }
}

@Composable
private fun DetailField(label: String, value: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 20.dp),
    )
    Text(text = value, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 4.dp))
}

@Composable
private fun detailContentTypeLabel(contentType: ContentType): String = when (contentType) {
    ContentType.LINK -> stringResource(R.string.content_type_link)
    ContentType.TEXT -> stringResource(R.string.content_type_text)
    ContentType.IMAGE -> stringResource(R.string.content_type_image)
    ContentType.DOCUMENT -> stringResource(R.string.content_type_document)
}
