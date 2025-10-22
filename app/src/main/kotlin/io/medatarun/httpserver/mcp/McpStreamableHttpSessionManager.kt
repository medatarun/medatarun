package io.medatarun.httpserver.mcp

import io.modelcontextprotocol.kotlin.sdk.server.Server
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Coordinates the lifecycle of [McpStreamableHttpSession] sessions so the HTTP endpoints, SSE transport and
 * underlying MCP server all agree on the same state machine.
 */
internal class McpStreamableHttpSessionManager(
    private val mcpServerFactory: () -> Server,
) {
    // Multi-endpoint access means we need a thread-safe map shared between POST and SSE handlers.
    private val sessions = ConcurrentHashMap<String, McpStreamableHttpSession>()

    /**
     * Creates a session for a new MCP Communication channel
     */
    suspend fun createSession(): McpStreamableHttpSession {
        val sessionId = UUID.randomUUID().toString()
        val transport = McpStreamableHttpTransport(sessionId)
        val server = mcpServerFactory()
        server.connect(transport)

        // We eagerly wire back-pressure callbacks so stale sessions are culled if either side
        // closes unexpectedly (common when clients lose their SSE connection).
        val session = McpStreamableHttpSession(sessionId, server, transport)
        transport.onClose { sessions.remove(sessionId) }
        server.onClose { sessions.remove(sessionId) }

        sessions[sessionId] = session
        return session
    }

    fun getSession(id: String): McpStreamableHttpSession? = sessions[id]

    suspend fun closeSession(id: String): Boolean {
        val removed = sessions.remove(id)
        // Tear down both the MCP server and transport to avoid leaking coroutine scopes.
        removed?.close()
        return removed != null
    }
}