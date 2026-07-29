package app.xiguang.collection

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.xiguang.XiguangApplication
import app.xiguang.data.local.CollectionEntity
import app.xiguang.data.local.FolderEntity
import app.xiguang.domain.model.GroupMode
import app.xiguang.domain.model.Platform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class CollectionGroup(
    val key: String,
    val title: String,
    val count: Int,
    val depth: Int = 0,
    val platform: Platform? = null,
)

data class CollectionUiState(
    val mode: GroupMode = GroupMode.FOLDER,
    val totalCount: Int = 0,
    val groups: List<CollectionGroup> = emptyList(),
    val isEmpty: Boolean = true,
)

class CollectionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as XiguangApplication).collectionRepository
    private val mode = MutableStateFlow(GroupMode.FOLDER)

    val uiState = combine(
        repository.collections,
        repository.folders,
        mode,
    ) { collections, folders, groupMode ->
        CollectionUiState(
            mode = groupMode,
            totalCount = collections.size,
            groups = when (groupMode) {
                GroupMode.FOLDER -> groupByFolder(collections, folders)
                GroupMode.PLATFORM -> groupByPlatform(collections)
            },
            isEmpty = collections.isEmpty(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CollectionUiState(),
    )

    fun toggleMode() {
        mode.update {
            if (it == GroupMode.FOLDER) GroupMode.PLATFORM else GroupMode.FOLDER
        }
    }

    private fun groupByFolder(
        collections: List<CollectionEntity>,
        folders: List<FolderEntity>,
    ): List<CollectionGroup> {
        val counts = collections.groupingBy(CollectionEntity::folderId).eachCount()
        val byParent = folders.groupBy(FolderEntity::parentId)
        val groups = mutableListOf<CollectionGroup>()

        counts[null]?.takeIf { it > 0 }?.let { count ->
            groups += CollectionGroup(
                key = "unfiled",
                title = "未整理",
                count = count,
            )
        }

        byParent[null].orEmpty().forEach { parent ->
            val childFolders = byParent[parent.id].orEmpty()
            val directCount = counts[parent.id] ?: 0
            val childCount = childFolders.sumOf { counts[it.id] ?: 0 }
            groups += CollectionGroup(
                key = "folder-${parent.id}",
                title = parent.name,
                count = directCount + childCount,
            )
            childFolders.forEach { child ->
                groups += CollectionGroup(
                    key = "folder-${child.id}",
                    title = child.name,
                    count = counts[child.id] ?: 0,
                    depth = 1,
                )
            }
        }
        return groups
    }

    private fun groupByPlatform(collections: List<CollectionEntity>): List<CollectionGroup> {
        val counts = collections.groupingBy(CollectionEntity::platform).eachCount()
        return Platform.entries.mapNotNull { platform ->
            counts[platform.name]?.takeIf { it > 0 }?.let { count ->
                CollectionGroup(
                    key = "platform-${platform.name}",
                    title = platform.label,
                    count = count,
                    platform = platform,
                )
            }
        }
    }
}
