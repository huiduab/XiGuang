package app.xiguang.share

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.xiguang.data.local.FolderEntity
import app.xiguang.domain.model.FolderMutationResult
import app.xiguang.domain.model.SharedPayload
import app.xiguang.ui.folder.FolderCreationErrorDialog
import app.xiguang.ui.folder.NewRootFolderDialog

@Composable
fun ShareSaveScreen(
    payload: SharedPayload,
    folders: List<FolderEntity>,
    folderCreationError: FolderMutationResult?,
    onCancel: () -> Unit,
    onCreateFolder: (String) -> Unit,
    onDismissFolderCreationError: () -> Unit,
    onSave: (Long?) -> Unit,
) {
    var selectedFolderId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showCreateFolder by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 28.dp, end = 12.dp, top = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "隙光",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onCancel) {
                Icon(Icons.Outlined.Close, contentDescription = "取消收藏")
            }
        }

        Text(
            text = "收藏内容",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(start = 28.dp, end = 28.dp, top = 24.dp),
        )
        Text(
            text = payload.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 28.dp, end = 28.dp, top = 22.dp),
        )
        Row(
            modifier = Modifier.padding(start = 28.dp, end = 28.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Language,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = payload.platform.label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            payload.originalUrl?.let { url ->
                Text(
                    text = "  ·  $url",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 24.dp),
            color = MaterialTheme.colorScheme.outline,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 28.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "保存到",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = { showCreateFolder = true }) {
                Text(text = androidx.compose.ui.res.stringResource(app.xiguang.R.string.folder_add_root))
            }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                FolderChoiceRow(
                    label = "未整理",
                    selected = selectedFolderId == null,
                    depth = 0,
                    onClick = { selectedFolderId = null },
                )
            }
            items(folders, key = FolderEntity::id) { folder ->
                FolderChoiceRow(
                    label = folder.name,
                    selected = selectedFolderId == folder.id,
                    depth = if (folder.parentId == null) 0 else 1,
                    onClick = { selectedFolderId = folder.id },
                )
            }
        }

        Button(
            onClick = { onSave(selectedFolderId) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 18.dp)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Icon(Icons.Outlined.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("保存到收藏")
        }
    }
    if (showCreateFolder) {
        NewRootFolderDialog(
            onDismiss = { showCreateFolder = false },
            onConfirm = { name ->
                onCreateFolder(name)
                showCreateFolder = false
            },
        )
    }
    folderCreationError?.let { result ->
        FolderCreationErrorDialog(
            result = result,
            onDismiss = onDismissFolderCreationError,
        )
    }
}

@Composable
private fun FolderChoiceRow(
    label: String,
    selected: Boolean,
    depth: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                start = if (depth == 0) 24.dp else 58.dp,
                end = 20.dp,
                top = 13.dp,
                bottom = 13.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.FolderOpen,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        RadioButton(selected = selected, onClick = onClick)
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = if (depth == 0) 24.dp else 58.dp),
        color = MaterialTheme.colorScheme.outline,
    )
}
