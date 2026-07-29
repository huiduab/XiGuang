package app.xiguang.collection.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.xiguang.R
import app.xiguang.domain.model.ContentType
import app.xiguang.domain.model.ReadFilter
import app.xiguang.domain.model.SavedCollection

@Composable
fun CollectionSearchRoute(
    onBack: () -> Unit,
    onOpenCollection: (Long) -> Unit,
    viewModel: CollectionSearchViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    CollectionSearchScreen(
        state = state,
        onBack = onBack,
        onOpenCollection = onOpenCollection,
        onQueryChange = viewModel::updateQuery,
        onReadFilterChange = viewModel::updateReadFilter,
        onContentTypeChange = viewModel::updateContentType,
    )
}

@Composable
private fun CollectionSearchScreen(
    state: CollectionSearchUiState,
    onBack: () -> Unit,
    onOpenCollection: (Long) -> Unit,
    onQueryChange: (String) -> Unit,
    onReadFilterChange: (ReadFilter) -> Unit,
    onContentTypeChange: (ContentType?) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, stringResource(R.string.navigate_back))
            }
            Text(stringResource(R.string.collection_search_title), style = MaterialTheme.typography.headlineSmall)
        }
        OutlinedTextField(
            value = state.options.query,
            onValueChange = onQueryChange,
            label = { Text(stringResource(R.string.search_query_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        )
        FilterRow(
            readFilter = state.options.readFilter,
            contentType = state.options.contentType,
            onReadFilterChange = onReadFilterChange,
            onContentTypeChange = onContentTypeChange,
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        if (state.collections.isEmpty()) {
            Text(
                stringResource(R.string.search_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(28.dp),
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.collections, key = SavedCollection::id) { collection ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenCollection(collection.id) }
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                    ) {
                        Text(collection.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(
                            collection.platform.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
private fun FilterRow(
    readFilter: ReadFilter,
    contentType: ContentType?,
    onReadFilterChange: (ReadFilter) -> Unit,
    onContentTypeChange: (ContentType?) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Row {
            FilterChip(readFilter == ReadFilter.ALL, { onReadFilterChange(ReadFilter.ALL) }, { Text(stringResource(R.string.filter_all)) })
            FilterChip(readFilter == ReadFilter.UNREAD, { onReadFilterChange(ReadFilter.UNREAD) }, { Text(stringResource(R.string.filter_unread)) }, modifier = Modifier.padding(start = 8.dp))
            FilterChip(readFilter == ReadFilter.READ, { onReadFilterChange(ReadFilter.READ) }, { Text(stringResource(R.string.filter_read)) }, modifier = Modifier.padding(start = 8.dp))
        }
        Row(modifier = Modifier.padding(top = 8.dp)) {
            FilterChip(contentType == null, { onContentTypeChange(null) }, { Text(stringResource(R.string.filter_any_type)) })
            FilterChip(contentType == ContentType.LINK, { onContentTypeChange(ContentType.LINK) }, { Text(stringResource(R.string.content_type_link)) }, modifier = Modifier.padding(start = 8.dp))
            FilterChip(contentType == ContentType.IMAGE, { onContentTypeChange(ContentType.IMAGE) }, { Text(stringResource(R.string.content_type_image)) }, modifier = Modifier.padding(start = 8.dp))
        }
        Row(modifier = Modifier.padding(top = 8.dp)) {
            FilterChip(contentType == ContentType.TEXT, { onContentTypeChange(ContentType.TEXT) }, { Text(stringResource(R.string.content_type_text)) })
            FilterChip(contentType == ContentType.DOCUMENT, { onContentTypeChange(ContentType.DOCUMENT) }, { Text(stringResource(R.string.content_type_document)) }, modifier = Modifier.padding(start = 8.dp))
        }
    }
}
