package app.xiguang.collection.folder

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.xiguang.XiguangApplication
import app.xiguang.domain.model.FolderMutationResult
import app.xiguang.domain.model.ManagedFolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FolderManagementUiState(
    val folders: List<ManagedFolder> = emptyList(),
    val feedback: FolderMutationResult? = null,
)

class FolderManagementViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as XiguangApplication).collectionRepository
    private val feedback = MutableStateFlow<FolderMutationResult?>(null)

    val uiState = combine(repository.observeManagedFolders(), feedback) { folders, result ->
        FolderManagementUiState(folders = folders, feedback = result)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FolderManagementUiState(),
    )

    fun create(name: String, parentId: Long?) = launchMutation { repository.createFolder(name, parentId) }

    fun rename(id: Long, name: String) = launchMutation { repository.renameFolder(id, name) }

    fun move(id: Long, moveUp: Boolean) = launchMutation { repository.moveFolderWithinSiblings(id, moveUp) }

    fun delete(id: Long) = launchMutation { repository.deleteFolderIfEmpty(id) }

    fun clearFeedback() {
        feedback.value = null
    }

    private fun launchMutation(action: suspend () -> FolderMutationResult) {
        viewModelScope.launch { feedback.value = action() }
    }
}
