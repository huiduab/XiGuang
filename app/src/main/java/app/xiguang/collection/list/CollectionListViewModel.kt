package app.xiguang.collection.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.xiguang.XiguangApplication
import app.xiguang.domain.model.CollectionFilter
import app.xiguang.domain.model.FolderOption
import app.xiguang.domain.model.Platform
import app.xiguang.domain.model.SavedCollection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CollectionListUiState(
    val title: String = "",
    val collections: List<SavedCollection> = emptyList(),
    val folders: List<FolderOption> = emptyList(),
    val selectedIds: Set<Long> = emptySet(),
) {
    val isSelecting: Boolean get() = selectedIds.isNotEmpty()
}

class CollectionListViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {
    private val repository = (application as XiguangApplication).collectionRepository
    private val title = savedStateHandle.get<String>("title").orEmpty()
    private val filter = parseFilter(savedStateHandle.get<String>("groupKey").orEmpty())
    private val selectedIds = MutableStateFlow<Set<Long>>(emptySet())

    val uiState = combine(
        repository.observeCollections(filter),
        repository.observeFolderOptions(),
        selectedIds,
    ) { collections, folders, selection ->
        CollectionListUiState(
            title = title,
            collections = collections,
            folders = folders,
            selectedIds = selection.intersect(collections.mapTo(mutableSetOf(), SavedCollection::id)),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CollectionListUiState(title = title),
    )

    fun toggleSelection(id: Long) {
        selectedIds.update { selected ->
            if (id in selected) selected - id else selected + id
        }
    }

    fun clearSelection() {
        selectedIds.value = emptySet()
    }

    fun moveSelected(folderId: Long?) {
        applyToSelected { ids -> repository.moveCollections(ids, folderId) }
    }

    fun setSelectedReadState(isRead: Boolean) {
        applyToSelected { ids -> repository.updateReadState(ids, isRead) }
    }

    fun deleteSelected() {
        applyToSelected(repository::deleteCollections)
    }

    private fun applyToSelected(action: suspend (Set<Long>) -> Int) {
        val currentIds = selectedIds.value
        if (currentIds.isEmpty()) return
        viewModelScope.launch {
            action(currentIds)
            selectedIds.value = emptySet()
        }
    }

    private fun parseFilter(groupKey: String): CollectionFilter = when {
        groupKey == "unfiled" -> CollectionFilter.Unfiled
        groupKey.startsWith("folder-") -> groupKey.removePrefix("folder-").toLongOrNull()
            ?.let(CollectionFilter::Folder)
            ?: CollectionFilter.All
        groupKey.startsWith("platform-") -> Platform.entries
            .firstOrNull { it.name == groupKey.removePrefix("platform-") }
            ?.let(CollectionFilter::PlatformFilter)
            ?: CollectionFilter.All
        else -> CollectionFilter.All
    }
}
