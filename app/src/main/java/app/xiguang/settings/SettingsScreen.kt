package app.xiguang.settings

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.xiguang.R
import app.xiguang.XiguangApplication
import app.xiguang.domain.model.AppSettings
import app.xiguang.domain.model.DefaultOpenMode
import app.xiguang.domain.model.ThemePreference
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(val settings: AppSettings = AppSettings(), val total: Int = 0, val unread: Int = 0)
class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as XiguangApplication
    val uiState = combine(app.settingsRepository.settings, app.collectionRepository.observeCollections()) { settings, items -> SettingsUiState(settings, items.size, items.count { !it.isRead }) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())
    fun theme(value: ThemePreference) = viewModelScope.launch { app.settingsRepository.setTheme(value) }
    fun openMode(value: DefaultOpenMode) = viewModelScope.launch { app.settingsRepository.setDefaultOpenMode(value) }
    fun notifications(value: Boolean) = viewModelScope.launch { app.settingsRepository.setNotificationsEnabled(value) }
}
@Composable fun SettingsRoute(viewModel: SettingsViewModel = viewModel()) { val state by viewModel.uiState.collectAsState(); SettingsScreen(state, viewModel::theme, viewModel::openMode, viewModel::notifications) }
@Composable private fun SettingsScreen(state: SettingsUiState, onTheme: (ThemePreference)->Unit, onOpenMode: (DefaultOpenMode)->Unit, onNotifications:(Boolean)->Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.displayLarge)
        Text(stringResource(R.string.settings_theme), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top=24.dp))
        Row { ThemePreference.entries.forEach { value -> FilterChip(value == state.settings.theme, { onTheme(value) }, { Text(themeLabel(value)) }, modifier = Modifier.padding(end=8.dp)) } }
        Text(stringResource(R.string.settings_open_mode), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top=20.dp))
        Row { DefaultOpenMode.entries.forEach { value -> FilterChip(value == state.settings.defaultOpenMode, { onOpenMode(value) }, { Text(openLabel(value)) }, modifier = Modifier.padding(end=8.dp)) } }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top=20.dp)) { Text(stringResource(R.string.settings_notifications), modifier=Modifier.weight(1f)); Switch(state.settings.notificationsEnabled, onNotifications) }
        Text(stringResource(R.string.settings_statistics), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top=28.dp))
        Text(stringResource(R.string.settings_statistics_value, state.total, state.unread), modifier = Modifier.padding(top=8.dp))
        Text(stringResource(R.string.settings_about), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top=28.dp))
        Text(stringResource(R.string.settings_about_value), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top=8.dp))
    }
}
@Composable private fun themeLabel(value: ThemePreference) = stringResource(when(value){ThemePreference.SYSTEM->R.string.theme_system;ThemePreference.LIGHT->R.string.theme_light;ThemePreference.DARK->R.string.theme_dark})
@Composable private fun openLabel(value: DefaultOpenMode) = stringResource(if(value==DefaultOpenMode.IN_APP) R.string.open_mode_in_app else R.string.open_mode_external)
