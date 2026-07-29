package app.xiguang.collection.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.xiguang.XiguangApplication
import app.xiguang.domain.model.CollectionSearchOptions
import app.xiguang.domain.model.ContentType
import app.xiguang.domain.model.ReadFilter
import app.xiguang.domain.model.SavedCollection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class CollectionSearchUiState(
    val options: CollectionSearchOptions = CollectionSearchOptions(),
    val collections: List<SavedCollection> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class CollectionSearchViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as XiguangApplication).collectionRepository
    private val options = MutableStateFlow(CollectionSearchOptions())

    val uiState = combine(
        options,
        options.flatMapLatest { current -> repository.observeCollections(searchOptions = current) },
    ) { currentOptions, collections ->
        CollectionSearchUiState(currentOptions, collections)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CollectionSearchUiState(),
    )

    fun updateQuery(query: String) {
        options.update { it.copy(query = query) }
    }

    fun updateReadFilter(readFilter: ReadFilter) {
        options.update { it.copy(readFilter = readFilter) }
    }

    fun updateContentType(contentType: ContentType?) {
        options.update { it.copy(contentType = contentType) }
    }
}
