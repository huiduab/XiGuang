package app.xiguang.collection.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.xiguang.XiguangApplication
import app.xiguang.domain.model.SavedCollection
import app.xiguang.domain.model.DefaultOpenMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CollectionDetailUiState(
    val collection: SavedCollection? = null,
    val isMissing: Boolean = false,
    val defaultOpenMode: DefaultOpenMode = DefaultOpenMode.IN_APP,
)

class CollectionDetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {
    private val app = application as XiguangApplication
    private val repository = app.collectionRepository
    private val collectionId = savedStateHandle.get<Long>("collectionId") ?: -1L

    val uiState = combine(repository.observeCollection(collectionId), app.settingsRepository.settings) { collection, settings ->
        CollectionDetailUiState(collection, collection == null, settings.defaultOpenMode)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CollectionDetailUiState(),
        )

    fun markReadAndOpen(onOpen: (Long) -> Unit) {
        if (collectionId < 0) return
        viewModelScope.launch {
            repository.updateReadState(setOf(collectionId), isRead = true)
            onOpen(collectionId)
        }
    }

    fun delete(onDeleted: () -> Unit) {
        if (collectionId < 0) return
        viewModelScope.launch {
            if (repository.deleteCollections(setOf(collectionId)) > 0) onDeleted()
        }
    }
}
