package ai.opennote.android.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/** Shape of GET /api/notebooks (api/models.py NotebookResponse). */
@Serializable
data class NotebookDto(
    val id: String,
    val name: String = "",
    val description: String = "",
    val archived: Boolean = false,
    val created: String = "",
    val updated: String = "",
    @SerialName("source_count") val sourceCount: Int = 0,
    @SerialName("note_count") val noteCount: Int = 0,
)

/** Shape of GET /api/notes (api/models.py NoteResponse). */
@Serializable
data class NoteDto(
    val id: String,
    val title: String? = null,
    val content: String? = null,
    @SerialName("note_type") val noteType: String? = null,
    val created: String = "",
    val updated: String = "",
)

/** Shape of GET /api/sources (api/models.py SourceListResponse), trimmed to what the client shows. */
@Serializable
data class SourceDto(
    val id: String,
    val title: String? = null,
    val topics: List<String>? = null,
    val embedded: Boolean = false,
    @SerialName("insights_count") val insightsCount: Int = 0,
    val created: String = "",
    val updated: String = "",
    val status: String? = null,
)

@Serializable
data class CreateNotebookRequest(
    val name: String,
    val description: String = "",
)

@Serializable
data class CreateNoteRequest(
    val content: String,
    @SerialName("notebook_id") val notebookId: String? = null,
    val title: String? = null,
    @SerialName("note_type") val noteType: String = "human",
)

@Serializable
data class SearchRequest(
    val query: String,
    @SerialName("notebook_id") val notebookId: String? = null,
    val type: String = "text",
    val limit: Int = 25,
    @SerialName("search_sources") val searchSources: Boolean = true,
    @SerialName("search_notes") val searchNotes: Boolean = true,
)

/**
 * /api/search returns List[Dict[str, Any]] — the keys vary by search type, so the
 * payload is kept as raw JsonObject and mapped defensively in [SearchHit.from].
 */
@Serializable
data class SearchResponse(
    val results: List<JsonObject> = emptyList(),
    @SerialName("total_count") val totalCount: Int = 0,
    @SerialName("search_type") val searchType: String = "",
)

data class SearchHit(
    val title: String,
    val snippet: String,
    val kind: String,
) {
    companion object {
        fun from(obj: JsonObject): SearchHit {
            val title = obj.string("title")
                ?: obj.string("name")
                ?: obj.string("id")
                ?: "Untitled"
            val snippet = obj.string("content")
                ?: obj.string("text")
                ?: obj.string("description")
                ?: ""
            val kind = obj.string("type")
                ?: obj.string("match_type")
                ?: obj.string("result_type")
                ?: ""
            return SearchHit(title = title, snippet = snippet.trim(), kind = kind)
        }

        private fun JsonObject.string(key: String): String? {
            val primitive = this[key] as? JsonPrimitive ?: return null
            if (primitive is JsonNull) return null
            return primitive.contentOrNull?.takeIf { it.isNotBlank() }
        }
    }
}

/** Shape of GET /api/auth/status (api/routers/auth.py). */
@Serializable
data class AuthStatus(
    @SerialName("auth_enabled") val authEnabled: Boolean = false,
    val message: String = "",
)

/** Server address + shared password, as entered on the connect screen. */
data class ServerSettings(
    val baseUrl: String = DEFAULT_BASE_URL,
    val password: String = "",
) {
    companion object {
        /** 10.0.2.2 is the host machine as seen from the Android emulator. */
        const val DEFAULT_BASE_URL = "http://10.0.2.2:5055"
    }
}
