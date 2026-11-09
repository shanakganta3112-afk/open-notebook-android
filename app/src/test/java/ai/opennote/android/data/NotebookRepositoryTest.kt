package ai.opennote.android.data

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotebookRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: NotebookRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = NotebookRepository.forSettings(
            ServerSettings(baseUrl = server.url("/").toString(), password = "s3cret"),
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun jsonResponse(body: String, code: Int = 200): MockResponse =
        MockResponse()
            .setResponseCode(code)
            .setHeader("Content-Type", "application/json")
            .setBody(body)

    @Test
    fun `normalizeBaseUrl adds scheme and trailing slash`() {
        assertEquals("http://host:5055/", ApiFactory.normalizeBaseUrl("host:5055"))
        assertEquals("http://host:5055/", ApiFactory.normalizeBaseUrl("http://host:5055"))
        assertEquals("https://host/", ApiFactory.normalizeBaseUrl("https://host/"))
        assertEquals(ServerSettings.DEFAULT_BASE_URL + "/", ApiFactory.normalizeBaseUrl(""))
    }

    @Test
    fun `notebooks sends bearer token and parses the payload`() = runBlocking {
        server.enqueue(
            jsonResponse(
                """
                [{"id":"notebook:1","name":"Research","description":"d","archived":false,
                  "created":"2026-01-01T00:00:00Z","updated":"2026-01-02T00:00:00Z",
                  "source_count":3,"note_count":2}]
                """.trimIndent(),
            ),
        )

        val result = repository.notebooks()

        assertTrue(result.isSuccess)
        val notebooks = result.getOrThrow()
        assertEquals(1, notebooks.size)
        assertEquals("Research", notebooks[0].name)
        assertEquals(3, notebooks[0].sourceCount)
        assertEquals(2, notebooks[0].noteCount)

        val recorded = server.takeRequest()
        assertEquals("/api/notebooks", recorded.path)
        assertEquals("Bearer s3cret", recorded.getHeader("Authorization"))
    }

    @Test
    fun `no authorization header when no password is set`() = runBlocking {
        val anonymous = NotebookRepository.forSettings(
            ServerSettings(baseUrl = server.url("/").toString(), password = ""),
        )
        server.enqueue(jsonResponse("[]"))

        anonymous.notebooks()

        assertNull(server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `401 becomes a failure with an actionable message`() = runBlocking {
        server.enqueue(jsonResponse("""{"detail":"Invalid password"}""", code = 401))

        val result = repository.notebooks()

        assertTrue(result.isFailure)
        val message = result.exceptionOrNull()!!.friendlyMessage()
        assertTrue(message.contains("Authentication failed"))
    }

    @Test
    fun `sources passes the notebook filter`() = runBlocking {
        server.enqueue(
            jsonResponse(
                """[{"id":"source:1","title":"Paper","embedded":true,"insights_count":4,
                     "created":"","updated":"","status":"completed"}]""",
            ),
        )

        val result = repository.sources("notebook:1")

        assertTrue(result.isSuccess)
        assertEquals("Paper", result.getOrThrow()[0].title)
        assertEquals(4, result.getOrThrow()[0].insightsCount)
        val path = server.takeRequest().path.orEmpty()
        assertTrue(path.startsWith("/api/sources?"))
        assertTrue(path.contains("notebook_id=notebook"))
        assertTrue(path.contains("limit=100"))
    }

    @Test
    fun `addNote posts the notebook id`() = runBlocking {
        server.enqueue(
            jsonResponse(
                """{"id":"note:1","title":null,"content":"hello","note_type":"human",
                    "created":"","updated":""}""",
            ),
        )

        val result = repository.addNote("notebook:1", "hello")

        assertTrue(result.isSuccess)
        assertEquals("hello", result.getOrThrow().content)
        val recorded = server.takeRequest()
        assertEquals("/api/notes", recorded.path)
        val body = recorded.body.readUtf8()
        assertTrue(body.contains("\"notebook_id\":\"notebook:1\""))
        assertTrue(body.contains("\"content\":\"hello\""))
    }

    @Test
    fun `search maps loosely typed results into hits`() = runBlocking {
        server.enqueue(
            jsonResponse(
                """
                {"results":[
                  {"id":"source:1","title":"Pricing","content":"cost per seat","type":"source"},
                  {"id":"note:9","text":"a note body","match_type":"note"}
                ],"total_count":2,"search_type":"text"}
                """.trimIndent(),
            ),
        )

        val result = repository.search("notebook:1", "pricing")

        assertTrue(result.isSuccess)
        val hits = result.getOrThrow()
        assertEquals(2, hits.size)
        assertEquals("Pricing", hits[0].title)
        assertEquals("cost per seat", hits[0].snippet)
        assertEquals("source", hits[0].kind)
        assertEquals("note:9", hits[1].title)
        assertEquals("a note body", hits[1].snippet)
        assertEquals("note", hits[1].kind)
    }

    @Test
    fun `testConnection reports whether auth is enabled`() = runBlocking {
        server.enqueue(jsonResponse("""{"auth_enabled":true,"message":"Authentication is required"}"""))

        val result = repository.testConnection()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().authEnabled)
        assertEquals("/api/auth/status", server.takeRequest().path)
    }
}
