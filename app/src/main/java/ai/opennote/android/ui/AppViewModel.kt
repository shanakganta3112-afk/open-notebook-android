package ai.opennote.android.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ai.opennote.android.data.NoteDto
import ai.opennote.android.data.NotebookDto
import ai.opennote.android.data.NotebookRepository
import ai.opennote.android.data.SearchHit
import ai.opennote.android.data.ServerSettings
import ai.opennote.android.data.SettingsStore
import ai.opennote.android.data.SourceDto
import ai.opennote.android.data.friendlyMessage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val store = SettingsStore(application)

    var settings by mutableStateOf(ServerSettings())
        private set

    var connecting by mutableStateOf(false)
        private set
    var connectionMessage by mutableStateOf<String?>(null)
        private set

    var notebooks by mutableStateOf<List<NotebookDto>>(emptyList())
        private set
    var loadingNotebooks by mutableStateOf(false)
        private set
    var notebooksError by mutableStateOf<String?>(null)
        private set

    var currentNotebook by mutableStateOf<NotebookDto?>(null)
        private set
    var notes by mutableStateOf<List<NoteDto>>(emptyList())
        private set
    var sources by mutableStateOf<List<SourceDto>>(emptyList())
        private set
    var loadingDetail by mutableStateOf(false)
        private set
    var detailError by mutableStateOf<String?>(null)
        private set

    var searchHits by mutableStateOf<List<SearchHit>>(emptyList())
        private set
    var searching by mutableStateOf(false)
        private set
    var searchError by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            settings = store.settings.first()
        }
    }

    private fun repository(): NotebookRepository = NotebookRepository.forSettings(settings)

    /** Updates the in-memory settings synchronously so the next call uses them. */
    fun apply(baseUrl: String, password: String) {
        val updated = ServerSettings(baseUrl = baseUrl.trim(), password = password)
        settings = updated
        viewModelScope.launch { store.save(updated) }
    }

    fun testConnection() {
        viewModelScope.launch {
            connecting = true
            connectionMessage = null
            val result = repository().testConnection()
            connectionMessage = result.fold(
                onSuccess = { status ->
                    if (status.authEnabled) {
                        "Connected. Server requires a password."
                    } else {
                        "Connected. Server authentication is disabled."
                    }
                },
                onFailure = { "Failed: ${it.friendlyMessage()}" },
            )
            connecting = false
        }
    }

    fun loadNotebooks() {
        viewModelScope.launch {
            loadingNotebooks = true
            notebooksError = null
            repository().notebooks()
                .onSuccess { notebooks = it }
                .onFailure { notebooksError = it.friendlyMessage() }
            loadingNotebooks = false
        }
    }

    fun createNotebook(name: String, onCreated: () -> Unit) {
        if (name.isBlank()) return
        viewModelScope.launch {
            notebooksError = null
            repository().createNotebook(name)
                .onSuccess {
                    onCreated()
                    loadNotebooks()
                }
                .onFailure { notebooksError = it.friendlyMessage() }
        }
    }

    fun openNotebook(notebook: NotebookDto) {
        currentNotebook = notebook
        notes = emptyList()
        sources = emptyList()
        searchHits = emptyList()
        searchError = null
        detailError = null
        viewModelScope.launch {
            loadingDetail = true
            val repo = repository()
            repo.notes(notebook.id)
                .onSuccess { notes = it }
                .onFailure { detailError = it.friendlyMessage() }
            repo.sources(notebook.id)
                .onSuccess { sources = it }
                .onFailure { detailError = detailError ?: it.friendlyMessage() }
            loadingDetail = false
        }
    }

    fun addNote(content: String, onAdded: () -> Unit) {
        val notebook = currentNotebook ?: return
        if (content.isBlank()) return
        viewModelScope.launch {
            detailError = null
            repository().addNote(notebook.id, content)
                .onSuccess {
                    onAdded()
                    repository().notes(notebook.id).onSuccess { notes = it }
                }
                .onFailure { detailError = it.friendlyMessage() }
        }
    }

    fun search(query: String) {
        val notebook = currentNotebook ?: return
        if (query.isBlank()) return
        viewModelScope.launch {
            searching = true
            searchError = null
            searchHits = emptyList()
            repository().search(notebook.id, query)
                .onSuccess { searchHits = it }
                .onFailure { searchError = it.friendlyMessage() }
            searching = false
        }
    }
}
