package app.xiguang.collection.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import app.xiguang.domain.model.FolderOption
import app.xiguang.ui.folder.FolderCreationErrorDialog
import app.xiguang.ui.folder.NewRootFolderDialog

@Composable
fun CollectionEditRoute(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: CollectionEditViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    CollectionEditScreen(
        state = state,
        onBack = onBack,
        onTitleChange = viewModel::updateTitle,
        onNoteChange = viewModel::updateNote,
        onFolderChange = viewModel::updateFolder,
        onCreateFolder = viewModel::createRootFolder,
        onDismissFolderCreationError = viewModel::clearFolderCreationError,
        onSave = { viewModel.save(onSaved) },
    )
}

@Composable
private fun CollectionEditScreen(
    state: CollectionEditUiState,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onFolderChange: (Long?) -> Unit,
    onCreateFolder: (String) -> Unit,
    onDismissFolderCreationError: () -> Unit,
    onSave: () -> Unit,
) {
    var showFolders by remember { mutableStateOf(false) }
    var showCreateFolder by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, stringResource(R.string.navigate_back))
            }
            Text(stringResource(R.string.edit_collection_title), style = MaterialTheme.typography.headlineSmall)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                label = { Text(stringResource(R.string.edit_title_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = state.note,
                onValueChange = onNoteChange,
                label = { Text(stringResource(R.string.edit_note_label)) },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                minLines = 3,
            )
            Text(stringResource(R.string.detail_folder), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 20.dp))
            TextButton(onClick = { showFolders = true }) {
                Text(state.folders.firstOrNull { it.id == state.folderId }?.path ?: stringResource(R.string.unfiled))
            }
            Button(
                onClick = onSave,
                enabled = state.title.isNotBlank() && state.collection != null,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            ) { Text(stringResource(R.string.save_action)) }
        }
    }
    if (showFolders) {
        EditFolderDialog(
            folders = state.folders,
            onDismiss = { showFolders = false },
            onCreate = {
                showFolders = false
                showCreateFolder = true
            },
            onSelect = { folderId ->
                onFolderChange(folderId)
                showFolders = false
            },
        )
    }
    if (showCreateFolder) {
        NewRootFolderDialog(
            onDismiss = {
                showCreateFolder = false
                showFolders = true
            },
            onConfirm = { name ->
                onCreateFolder(name)
                showCreateFolder = false
                showFolders = true
            },
        )
    }
    state.folderCreationError?.let { result ->
        FolderCreationErrorDialog(
            result = result,
            onDismiss = onDismissFolderCreationError,
        )
    }
}

@Composable
private fun EditFolderDialog(
    folders: List<FolderOption>,
    onDismiss: () -> Unit,
    onCreate: () -> Unit,
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
        confirmButton = {
            TextButton(onClick = onCreate) {
                Text(stringResource(R.string.folder_add_root))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_action))
            }
        },
    )
}
