package app.xiguang.collection.list

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.xiguang.R
import app.xiguang.domain.model.ContentType
import app.xiguang.domain.model.SavedCollection
import java.text.DateFormat
import java.util.Date

@Composable
fun CollectionListRoute(
    onBack: () -> Unit,
    onOpenCollection: (Long) -> Unit,
    viewModel: CollectionListViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    CollectionListScreen(
        state = state,
        onBack = onBack,
        onOpenCollection = onOpenCollection,
        onToggleSelection = viewModel::toggleSelection,
        onClearSelection = viewModel::clearSelection,
        onMoveSelected = viewModel::moveSelected,
        onSetSelectedReadState = viewModel::setSelectedReadState,
        onDeleteSelected = viewModel::deleteSelected,
    )
}

@Composable
internal fun CollectionListScreen(
    state: CollectionListUiState,
    onBack: () -> Unit,
    onOpenCollection: (Long) -> Unit,
    onToggleSelection: (Long) -> Unit = {},
    onClearSelection: () -> Unit = {},
    onMoveSelected: (Long?) -> Unit = {},
    onSetSelectedReadState: (Boolean) -> Unit = {},
    onDeleteSelected: () -> Unit = {},
) {
    var showMoveDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize()) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { if (state.isSelecting) onClearSelection() else onBack() }) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_back),
                )
            }
            Text(
                text = if (state.isSelecting) {
                    stringResource(R.string.selected_count, state.selectedIds.size)
                } else {
                    state.title.ifBlank { stringResource(R.string.collection_list_title) }
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            if (state.isSelecting) {
                TextButton(onClick = { onSetSelectedReadState(true) }) { Text(stringResource(R.string.mark_read)) }
                TextButton(onClick = { onSetSelectedReadState(false) }) { Text(stringResource(R.string.mark_unread)) }
                TextButton(onClick = { showMoveDialog = true }) { Text(stringResource(R.string.move_action)) }
                TextButton(onClick = { showDeleteDialog = true }) { Text(stringResource(R.string.delete_action)) }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        if (state.collections.isEmpty()) {
            EmptyCollectionList()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(state.collections, key = SavedCollection::id) { collection ->
                    CollectionListItem(
                        collection = collection,
                        selected = collection.id in state.selectedIds,
                        isSelecting = state.isSelecting,
                        onClick = {
                            if (state.isSelecting) onToggleSelection(collection.id) else onOpenCollection(collection.id)
                        },
                        onLongClick = { onToggleSelection(collection.id) },
                    )
                }
            }
        }
    }
    if (showMoveDialog) {
        FolderSelectionDialog(
            folders = state.folders,
            onDismiss = { showMoveDialog = false },
            onSelect = { folderId ->
                onMoveSelected(folderId)
                showMoveDialog = false
            },
        )
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_collections_title)) },
            text = { Text(stringResource(R.string.delete_collections_message, state.selectedIds.size)) },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteSelected()
                    showDeleteDialog = false
                }) { Text(stringResource(R.string.delete_action)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.cancel_action)) }
            },
        )
    }
}

@Composable
private fun EmptyCollectionList() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.BookmarkBorder,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.collection_list_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = stringResource(R.string.collection_list_empty_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun CollectionListItem(
    collection: SavedCollection,
    selected: Boolean,
    isSelecting: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isSelecting) Checkbox(checked = selected, onCheckedChange = { onClick() })
        Column(modifier = Modifier.weight(1f)) {
        Text(
            text = collection.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        collection.sharedText?.takeIf(String::isNotBlank)?.let { sharedText ->
            Text(
                text = sharedText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 5.dp),
            )
        }
        Text(
            text = listOf(
                collection.platform.label,
                contentTypeLabel(collection.contentType),
                DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(collection.createdAt)),
                if (collection.isRead) stringResource(R.string.read_state_read) else stringResource(R.string.read_state_unread),
            ).joinToString(" · "),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 7.dp),
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 16.dp),
            color = MaterialTheme.colorScheme.outline,
        )
        }
    }
}

@Composable
private fun FolderSelectionDialog(
    folders: List<app.xiguang.domain.model.FolderOption>,
    onDismiss: () -> Unit,
    onSelect: (Long?) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.move_to_folder_title)) },
        text = {
            Column {
                TextButton(onClick = { onSelect(null) }) { Text(stringResource(R.string.unfiled)) }
                folders.forEach { folder ->
                    TextButton(onClick = { onSelect(folder.id) }) { Text(folder.path) }
                }
            }
        },
        confirmButton = {},
    )
}

@Composable
private fun contentTypeLabel(contentType: ContentType): String = when (contentType) {
    ContentType.LINK -> stringResource(R.string.content_type_link)
    ContentType.TEXT -> stringResource(R.string.content_type_text)
    ContentType.IMAGE -> stringResource(R.string.content_type_image)
    ContentType.DOCUMENT -> stringResource(R.string.content_type_document)
}
