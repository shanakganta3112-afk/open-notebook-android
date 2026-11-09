package ai.opennote.android.data

import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/** Thin, testable wrapper over [OpenNotebookApi]: every call returns a [Result]. */
class NotebookRepository(private val api: OpenNotebookApi) {

    suspend fun testConnection(): Result<AuthStatus> = runCatching { api.authStatus() }

    suspend fun notebooks(): Result<List<NotebookDto>> = runCatching { api.notebooks() }

    suspend fun createNotebook(name: String): Result<NotebookDto> =
        runCatching { api.createNotebook(CreateNotebookRequest(name = name.trim())) }

    suspend fun notes(notebookId: String): Result<List<NoteDto>> =
        runCatching { api.notes(notebookId = notebookId) }

    suspend fun sources(notebookId: String): Result<List<SourceDto>> =
        runCatching { api.sources(notebookId = notebookId) }

    suspend fun addNote(notebookId: String, content: String): Result<NoteDto> =
        runCatching { api.createNote(CreateNoteRequest(content = content, notebookId = notebookId)) }

    suspend fun search(notebookId: String, query: String): Result<List<SearchHit>> = runCatching {
        val response = api.search(SearchRequest(query = query, notebookId = notebookId))
        response.results.map { SearchHit.from(it) }
    }

    companion object {
        fun forSettings(settings: ServerSettings): NotebookRepository =
            NotebookRepository(ApiFactory.create(settings))
    }
}

/** Turns transport and HTTP failures into something a user can act on. */
fun Throwable.friendlyMessage(): String = when (this) {
    is HttpException -> when (code()) {
        401, 403 -> "Authentication failed (HTTP ${code()}). Check the password."
        404 -> "Not found (HTTP 404). Check the server address and path."
        422 -> "The server rejected the request (HTTP 422)."
        in 500..599 -> "Server error (HTTP ${code()}). Check the Open Notebook logs."
        else -> "Request failed (HTTP ${code()})."
    }
    is UnknownHostException -> "Unknown host. Check the server address."
    is ConnectException -> "Connection refused. Is Open Notebook running on that address?"
    is SocketTimeoutException -> "Timed out waiting for the server."
    is IOException -> message ?: "Network error."
    else -> message ?: this::class.java.simpleName
}
