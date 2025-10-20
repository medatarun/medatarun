package io.medatarun.httpserver.mcp

import io.modelcontextprotocol.kotlin.sdk.server.Server
import org.slf4j.LoggerFactory

/**
 * Each HTTP/SSE session owns its own MCP Server so transport and business logic share a lifecycle.
 * When the session ends we close both sides, keeping client state isolated and avoiding coroutine
 * leaks; the setup is cheap unless you expect thousands of simultaneous sessions or extreme churn.
 */
internal class McpStreamableHttpSession(
    val id: String,
    private val mcpServer: Server,
    val mcpTransport: McpStreamableHttpTransport,
) {
    suspend fun close() {
        try {
            mcpServer.close()
        } catch (t: Throwable) {
            logger.warn("Failed to close MCP server for session {}", id, t)
        }

        try {
            mcpTransport.close()
        } catch (t: Throwable) {
            logger.warn("Failed to close MCP transport for session {}", id, t)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(McpStreamableHttpSession::class.java)
    }
}