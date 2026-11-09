package ai.opennote.android.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * The subset of the Open Notebook REST API this client uses.
 * Routes mirror the routers in api/routers, all mounted under the /api prefix (api/main.py).
 */
interface OpenNotebookApi {

    @GET("api/auth/status")
    suspend fun authStatus(): AuthStatus

    @GET("api/notebooks")
    suspend fun notebooks(): List<NotebookDto>

    @POST("api/notebooks")
    suspend fun createNotebook(@Body body: CreateNotebookRequest): NotebookDto

    @GET("api/notebooks/{notebookId}")
    suspend fun notebook(@Path("notebookId") notebookId: String): NotebookDto

    @GET("api/notes")
    suspend fun notes(@Query("notebook_id") notebookId: String? = null): List<NoteDto>

    @POST("api/notes")
    suspend fun createNote(@Body body: CreateNoteRequest): NoteDto

    @GET("api/sources")
    suspend fun sources(
        @Query("notebook_id") notebookId: String? = null,
        @Query("limit") limit: Int = 100,
    ): List<SourceDto>

    @POST("api/search")
    suspend fun search(@Body body: SearchRequest): SearchResponse
}
