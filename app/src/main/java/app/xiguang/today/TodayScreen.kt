package app.xiguang.today

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.xiguang.R
import app.xiguang.XiguangApplication
import app.xiguang.domain.model.SavedCollection
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class TodayUiState(val unread: List<SavedCollection> = emptyList(), val addedToday: List<SavedCollection> = emptyList(), val random: SavedCollection? = null)

class TodayViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as XiguangApplication).collectionRepository
    val uiState = repository.observeCollections().map { items ->
        val start = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
        TodayUiState(items.filterNot(SavedCollection::isRead), items.filter { it.createdAt >= start }, items.randomOrNull())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState())
}

@Composable
fun TodayRoute(onOpenCollection: (Long) -> Unit, viewModel: TodayViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text(stringResource(R.string.today_title), style = MaterialTheme.typography.displayLarge)
        state.random?.let { random -> Button(onClick = { onOpenCollection(random.id) }, modifier = Modifier.padding(top = 16.dp)) { Text(stringResource(R.string.today_random_read)) } }
        TodaySection(stringResource(R.string.today_unread), state.unread, onOpenCollection)
        TodaySection(stringResource(R.string.today_added), state.addedToday, onOpenCollection)
    }
}

@Composable private fun TodaySection(title: String, items: List<SavedCollection>, onOpen: (Long) -> Unit) {
    Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 24.dp, bottom = 8.dp))
    if (items.isEmpty()) Text(stringResource(R.string.today_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
    else LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) { items(items, key = SavedCollection::id) { item ->
        Row(Modifier.fillMaxWidth().clickable { onOpen(item.id) }.padding(vertical = 12.dp)) { Text(item.title, modifier = Modifier.weight(1f)); Text(item.platform.label, color = MaterialTheme.colorScheme.primary) }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    } }
}
