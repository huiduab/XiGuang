package app.xiguang.collection.edit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.xiguang.XiguangApplication
import app.xiguang.domain.model.CollectionEditInput
import app.xiguang.domain.model.FolderMutationResult
import app.xiguang.domain.model.FolderOption
import app.xiguang.domain.model.SavedCollection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CollectionEditUiState(
    val collection: SavedCollection? = null,
    val folders: List<FolderOption> = emptyList(),
    val title: String = "",
    val note: String = "",
    val folderId: Long? = null,
    val folderCreationError: FolderMutationResult? = null,
)

private data class CollectionEditDraft(
    val collectionId: Long,
    val title: String,
    val note: String,
    val folderId: Long?,
)

class CollectionEditViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {
    private val repository = (application as XiguangApplication).collectionRepository
    private val collectionId = savedStateHandle.get<Long>("collectionId") ?: -1L
    private val draft = MutableStateFlow<CollectionEditDraft?>(null)
    private val folderCreationError = MutableStateFlow<FolderMutationResult?>(null)

    val uiState = combine(
        repository.observeCollection(collectionId),
        repository.observeFolderOptions(),
        draft,
        folderCreationError,
    ) { collection, folders, currentDraft, creationError ->
        val effectiveDraft = currentDraft?.takeIf { it.collectionId == collection?.id }
        if (effectiveDraft == null) {
            CollectionEditUiState(
                collection = collection,
                folders = folders,
                title = collection?.title.orEmpty(),
                note = collection?.note.orEmpty(),
                folderId = collection?.folderId,
                folderCreationError = creationError,
            )
        } else {
            CollectionEditUiState(
                collection = collection,
                folders = folders,
                title = effectiveDraft.title,
                note = effectiveDraft.note,
                folderId = effectiveDraft.folderId,
                folderCreationError = creationError,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CollectionEditUiState(),
    )

    fun updateTitle(value: String) = updateDraft { it.copy(title = value) }

    fun updateNote(value: String) = updateDraft { it.copy(note = value) }

    fun updateFolder(folderId: Long?) = updateDraft { it.copy(folderId = folderId) }

    fun createRootFolder(name: String) {
        viewModelScope.launch {
            folderCreationError.value = repository.createFolder(name, parentId = null)
                .takeUnless { it == FolderMutationResult.Success }
        }
    }

    fun clearFolderCreationError() {
        folderCreationError.value = null
    }

    fun save(onSaved: () -> Unit) {
        val current = uiState.value
        if (collectionId < 0 || current.title.isBlank()) return
        viewModelScope.launch {
            if (repository.updateCollection(collectionId, CollectionEditInput(current.title, current.note, current.folderId))) {
                onSaved()
            }
        }
    }

    private fun updateDraft(transform: (CollectionEditDraft) -> CollectionEditDraft) {
        val current = uiState.value
        val currentId = current.collection?.id ?: return
        draft.value = transform(CollectionEditDraft(currentId, current.title, current.note, current.folderId))
    }
}
