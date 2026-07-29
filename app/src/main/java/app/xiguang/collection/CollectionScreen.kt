package app.xiguang.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.xiguang.domain.model.GroupMode
import app.xiguang.domain.model.Platform
import app.xiguang.ui.theme.xiguangAccents

@Composable
fun CollectionRoute(
    onSearch: () -> Unit,
    onOpenGroup: (CollectionGroup) -> Unit,
    onManageFolders: () -> Unit,
    viewModel: CollectionViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CollectionScreen(
        state = state,
        onToggleMode = viewModel::toggleMode,
        onSearch = onSearch,
        onOpenGroup = onOpenGroup,
        onManageFolders = onManageFolders,
    )
}

@Composable
private fun CollectionScreen(
    state: CollectionUiState,
    onToggleMode: () -> Unit,
    onSearch: () -> Unit,
    onOpenGroup: (CollectionGroup) -> Unit,
    onManageFolders: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        CollectionHeader(
            state = state,
            onToggleMode = onToggleMode,
            onSearch = onSearch,
            onManageFolders = onManageFolders,
        )

        if (state.isEmpty) {
            EmptyCollection(modifier = Modifier.weight(1f))
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(state.groups, key = CollectionGroup::key) { group ->
                    CollectionGroupRow(
                        group = group,
                        onClick = { onOpenGroup(group) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CollectionHeader(
    state: CollectionUiState,
    onToggleMode: () -> Unit,
    onSearch: () -> Unit,
    onManageFolders: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 28.dp, end = 18.dp, top = 22.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "隙光",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onSearch) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "搜索收藏",
                )
            }
            TextButton(onClick = onManageFolders) {
                Text(stringResource(app.xiguang.R.string.folder_manage_action))
            }
            IconButton(onClick = onToggleMode) {
                Icon(
                    imageVector = if (state.mode == GroupMode.FOLDER) {
                        Icons.Outlined.FolderOpen
                    } else {
                        Icons.Outlined.Language
                    },
                    contentDescription = if (state.mode == GroupMode.FOLDER) {
                        "当前按收藏夹分类，点击切换为平台分类"
                    } else {
                        "当前按平台分类，点击切换为收藏夹分类"
                    },
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Text(
            text = "收藏",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 22.dp, bottom = 8.dp),
        )
        Text(
            text = if (state.mode == GroupMode.FOLDER) {
                "按收藏夹分类 · ${state.totalCount} 条"
            } else {
                "按平台分类 · ${state.totalCount} 条"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp),
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun CollectionGroupRow(
    group: CollectionGroup,
    onClick: () -> Unit,
) {
    val tint = platformColor(group.platform)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                start = if (group.depth == 0) 28.dp else 64.dp,
                end = 24.dp,
                top = 19.dp,
                bottom = 19.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (group.platform == null) {
                Icons.Outlined.FolderOpen
            } else {
                Icons.Outlined.Language
            },
            contentDescription = null,
            tint = tint,
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = group.title,
            style = if (group.depth == 0) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.bodyLarge
            },
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = group.count.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = if (group.depth == 0) 28.dp else 64.dp),
        color = MaterialTheme.colorScheme.outline,
    )
}

@Composable
private fun EmptyCollection(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.BookmarkBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "把值得留下的内容带回来",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 18.dp),
            )
            Text(
                text = "在 X、微博、小红书、抖音或浏览器中点击分享，然后选择“收藏到隙光”。",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

@Composable
private fun platformColor(platform: Platform?): Color = when (platform) {
    Platform.X -> MaterialTheme.xiguangAccents.mineralBlue
    Platform.WEIBO -> MaterialTheme.xiguangAccents.oxidizedCopper
    Platform.XIAOHONGSHU -> MaterialTheme.xiguangAccents.plum
    Platform.DOUYIN -> MaterialTheme.colorScheme.onBackground
    Platform.BILIBILI -> MaterialTheme.xiguangAccents.plum
    Platform.ZHIHU -> MaterialTheme.xiguangAccents.mineralBlue
    Platform.YOUTUBE -> MaterialTheme.xiguangAccents.oxidizedCopper
    Platform.BLOG -> MaterialTheme.xiguangAccents.sage
    Platform.OTHER, null -> MaterialTheme.colorScheme.primary
}
