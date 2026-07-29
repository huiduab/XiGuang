package app.xiguang.ui.folder

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import app.xiguang.R
import app.xiguang.domain.model.FolderMutationResult

@Composable
fun NewRootFolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.folder_add_root)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.folder_name_label)) },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank(),
            ) {
                Text(stringResource(R.string.create_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_action))
            }
        },
    )
}

@Composable
fun FolderCreationErrorDialog(
    result: FolderMutationResult,
    onDismiss: () -> Unit,
) {
    if (result == FolderMutationResult.Success) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.folder_add_root)) },
        text = { Text(folderCreationError(result)) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ok_action))
            }
        },
    )
}

@Composable
private fun folderCreationError(result: FolderMutationResult): String = when (result) {
    FolderMutationResult.Success -> ""
    FolderMutationResult.InvalidName -> stringResource(R.string.folder_invalid_name)
    FolderMutationResult.DuplicateName -> stringResource(R.string.folder_duplicate_name)
    FolderMutationResult.ParentNotFound -> stringResource(R.string.folder_parent_not_found)
    FolderMutationResult.MaximumDepthReached -> stringResource(R.string.folder_max_depth)
    FolderMutationResult.NotEmpty -> stringResource(R.string.folder_not_empty)
    FolderMutationResult.NotFound -> stringResource(R.string.folder_not_found)
}
