package app.xiguang.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.xiguang.domain.model.AppSettings
import app.xiguang.domain.model.DefaultOpenMode
import app.xiguang.domain.model.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.xiguangSettingsDataStore by preferencesDataStore("xiguang_settings")

class SettingsRepository(private val context: Context) {
    private val themeKey = stringPreferencesKey("theme")
    private val openModeKey = stringPreferencesKey("default_open_mode")
    private val notificationsKey = booleanPreferencesKey("notifications_enabled")

    val settings: Flow<AppSettings> = context.xiguangSettingsDataStore.data.map { preferences ->
        AppSettings(
            theme = preferences[themeKey].enumOrDefault(ThemePreference.SYSTEM),
            defaultOpenMode = preferences[openModeKey].enumOrDefault(DefaultOpenMode.IN_APP),
            notificationsEnabled = preferences[notificationsKey] ?: false,
        )
    }

    suspend fun setTheme(value: ThemePreference) = context.xiguangSettingsDataStore.edit { it[themeKey] = value.name }
    suspend fun setDefaultOpenMode(value: DefaultOpenMode) = context.xiguangSettingsDataStore.edit { it[openModeKey] = value.name }
    suspend fun setNotificationsEnabled(value: Boolean) = context.xiguangSettingsDataStore.edit { it[notificationsKey] = value }

    private inline fun <reified T : Enum<T>> String?.enumOrDefault(default: T): T =
        enumValues<T>().firstOrNull { it.name == this } ?: default
}
