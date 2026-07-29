package app.xiguang.collection.folder

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.xiguang.R
import app.xiguang.domain.model.FolderMutationResult
import app.xiguang.domain.model.ManagedFolder

@Composable
fun FolderManagementRoute(
    onBack: () -> Unit,
    viewModel: FolderManagementViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    FolderManagementScreen(
        state = state,
        onBack = onBack,
        onCreate = viewModel::create,
        onRename = viewModel::rename,
        onMove = viewModel::move,
        onDelete = viewModel::delete,
        onDismissFeedback = viewModel::clearFeedback,
    )
}

@Composable
private fun FolderManagementScreen(
    state: FolderManagementUiState,
    onBack: () -> Unit,
    onCreate: (String, Long?) -> Unit,
    onRename: (Long, String) -> Unit,
    onMove: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit,
    onDismissFeedback: () -> Unit,
) {
    var createParentId by remember { mutableStateOf<Long?>(null) }
    var renameFolder by remember { mutableStateOf<ManagedFolder?>(null) }
    var deleteFolder by remember { mutableStateOf<ManagedFolder?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    val childrenByParent = state.folders.groupBy(ManagedFolder::parentId)
    val roots = childrenByParent[null].orEmpty().sortedBy(ManagedFolder::sortOrder)

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, stringResource(R.string.navigate_back))
            }
            Text(
                stringResource(R.string.folder_manage_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = {
                createParentId = null
                showCreateDialog = true
            }) { Text(stringResource(R.string.folder_add_root)) }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        if (roots.isEmpty()) {
            Text(
                stringResource(R.string.folder_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(28.dp),
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                roots.forEach { root ->
                    item(key = root.id) {
                        FolderRow(
                            folder = root,
                            depth = 0,
                            onCreateChild = {
                                createParentId = root.id
                                showCreateDialog = true
                            },
                            onRename = { renameFolder = root },
                            onMove = onMove,
                            onDelete = { deleteFolder = root },
                        )
                    }
                    items(childrenByParent[root.id].orEmpty().sortedBy(ManagedFolder::sortOrder), key = ManagedFolder::id) { child ->
                        FolderRow(
                            folder = child,
                            depth = 1,
                            onCreateChild = null,
                            onRename = { renameFolder = child },
                            onMove = onMove,
                            onDelete = { deleteFolder = child },
                        )
                    }
                }
            }
        }
    }
    if (showCreateDialog) {
        FolderNameDialog(
            title = if (createParentId == null) stringResource(R.string.folder_add_root) else stringResource(R.string.folder_add_child),
            initialName = "",
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                onCreate(name, createParentId)
                showCreateDialog = false
            },
        )
    }
    renameFolder?.let { folder ->
        FolderNameDialog(
            title = stringResource(R.string.folder_rename),
            initialName = folder.name,
            onDismiss = { renameFolder = null },
            onConfirm = { name ->
                onRename(folder.id, name)
                renameFolder = null
            },
        )
    }
    deleteFolder?.let { folder ->
        AlertDialog(
            onDismissRequest = { deleteFolder = null },
            title = { Text(stringResource(R.string.folder_delete_title)) },
            text = { Text(stringResource(R.string.folder_delete_message, folder.name)) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(folder.id)
                    deleteFolder = null
                }) { Text(stringResource(R.string.delete_action)) }
            },
            dismissButton = { TextButton(onClick = { deleteFolder = null }) { Text(stringResource(R.string.cancel_action)) } },
        )
    }
    state.feedback?.let { result ->
        AlertDialog(
            onDismissRequest = onDismissFeedback,
            title = { Text(stringResource(R.string.folder_manage_title)) },
            text = { Text(folderFeedback(result)) },
            confirmButton = { TextButton(onClick = onDismissFeedback) { Text(stringResource(R.string.ok_action)) } },
        )
    }
}

@Composable
private fun FolderRow(
    folder: ManagedFolder,
    depth: Int,
    onCreateChild: (() -> Unit)?,
    onRename: () -> Unit,
    onMove: (Long, Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(start = if (depth == 0) 20.dp else 52.dp, end = 20.dp, top = 12.dp, bottom = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(folder.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text("${folder.collectionCount}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row {
            onCreateChild?.let { TextButton(onClick = it) { Text(stringResource(R.string.folder_add_child)) } }
            TextButton(onClick = onRename) { Text(stringResource(R.string.folder_rename)) }
            TextButton(onClick = { onMove(folder.id, true) }) { Text(stringResource(R.string.folder_move_up)) }
            TextButton(onClick = { onMove(folder.id, false) }) { Text(stringResource(R.string.folder_move_down)) }
            TextButton(onClick = onDelete) { Text(stringResource(R.string.delete_action)) }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun FolderNameDialog(
    title: String,
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.folder_name_label)) }) },
        confirmButton = { TextButton(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) { Text(stringResource(R.string.save_action)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel_action)) } },
    )
}

@Composable
private fun folderFeedback(result: FolderMutationResult): String = when (result) {
    FolderMutationResult.Success -> stringResource(R.string.folder_mutation_success)
    FolderMutationResult.InvalidName -> stringResource(R.string.folder_invalid_name)
    FolderMutationResult.DuplicateName -> stringResource(R.string.folder_duplicate_name)
    FolderMutationResult.ParentNotFound -> stringResource(R.string.folder_parent_not_found)
    FolderMutationResult.MaximumDepthReached -> stringResource(R.string.folder_max_depth)
    FolderMutationResult.NotEmpty -> stringResource(R.string.folder_not_empty)
    FolderMutationResult.NotFound -> stringResource(R.string.folder_not_found)
}
