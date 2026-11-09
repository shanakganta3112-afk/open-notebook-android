package ai.opennote.android.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "open_notebook_settings")

/** Persists the server address and password between launches. */
class SettingsStore(private val context: Context) {

    val settings: Flow<ServerSettings> = context.settingsDataStore.data.map { prefs ->
        ServerSettings(
            baseUrl = prefs[KEY_BASE_URL] ?: ServerSettings.DEFAULT_BASE_URL,
            password = prefs[KEY_PASSWORD] ?: "",
        )
    }

    suspend fun save(settings: ServerSettings) {
        context.settingsDataStore.edit { prefs ->
            prefs[KEY_BASE_URL] = settings.baseUrl
            prefs[KEY_PASSWORD] = settings.password
        }
    }

    private companion object {
        val KEY_BASE_URL = stringPreferencesKey("base_url")
        val KEY_PASSWORD = stringPreferencesKey("password")
    }
}
