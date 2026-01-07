package io.medatarun.httpserver.mcp

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sse.*
import io.modelcontextprotocol.kotlin.sdk.JSONRPCMessage
import io.modelcontextprotocol.kotlin.sdk.JSONRPCRequest
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.shared.McpJson
import kotlinx.coroutines.TimeoutCancellationException
import org.slf4j.LoggerFactory

/**
 * Temporary bridge that exposes MCP over the [McpStreamableHttpTransport] transport while Ktor's native
 * module is still JSON-RPC SSE only. We keep the same surface as the future official module so
 * swapping back becomes trivial once upstream gains StreamableHttp support.
 */
class McpStreamableHttpBridge() {

    private val streamableSessions = McpStreamableHttpSessionManager()

    /**
     * Handles the SSE leg of a StreamableHTTP session: validates the session id, resumes delivery
     * of buffered events (honouring `Last-Event-ID` for replays), and hands control to the transport
     * so it can stream MCP notifications back to the client until the SSE disconnects.
     */
    suspend fun handleStreamableSse(session: ServerSSESession) {
        // Clients must reconnect with the same session id, otherwise we cannot recover the
        // transport state required to replay buffered events.

        val sessionId = session.call.request.headers[MCP_SESSION_ID_HEADER]
        if (sessionId.isNullOrBlank()) {
            session.call.respond(HttpStatusCode.BadRequest, "Missing MCP session header")
            return
        }

        val streamSession = streamableSessions.getSession(sessionId)
        if (streamSession == null) {
            session.call.respond(HttpStatusCode.Gone, "Unknown MCP session")
            return
        }

        val lastEventId = session.call.request.headers[LAST_EVENT_ID_HEADER]?.toLongOrNull()

        session.call.response.headers.append(HttpHeaders.CacheControl, "no-store")

        // We let the transport handle SSE attachments so it can restore inflight messages if
        // the client presented a Last-Event-ID during a reconnect (important for long streams).
        runCatching {
            streamSession.mcpTransport.attachSse(session, lastEventId)
        }.onFailure { throwable ->
            logger.warn("SSE stream failure for session $sessionId", throwable)
        }
    }


    /**
     * Handles the POST leg of StreamableHTTP: parses the JSON-RPC envelope, creates or resumes the
     * associated session, forwards the message to the transport, and returns either the immediate
     * JSON response or HTTP 202 when the answer will arrive asynchronously via SSE.
     */
    suspend fun handleStreamablePost(call: ApplicationCall, serverFactory: () -> Server) {
        // The POST leg bootstraps or continues the MCP conversation, so we do strict parsing
        // here to control error responses instead of delegating to Ktor's built-in pipeline.
        val rawBody = runCatching { call.receiveText() }.getOrElse {
            call.respond(HttpStatusCode.BadRequest, "Invalid request body")
            return
        }

        if (rawBody.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Empty request body")
            return
        }

        val message = runCatching { McpJson.decodeFromString<JSONRPCMessage>(rawBody) }.getOrElse { throwable ->
            logger.warn("Failed to parse MCP request", throwable)
            call.respond(HttpStatusCode.BadRequest, "Invalid MCP payload")
            return
        }

        val sessionIdHeader = call.request.headers[MCP_SESSION_ID_HEADER]
        val session = if (sessionIdHeader.isNullOrBlank()) {
            if (message !is JSONRPCRequest) {
                call.respond(HttpStatusCode.BadRequest, "New sessions must start with a JSON-RPC request")
                return
            }

            // No session id means the client is negotiating a brand new MCP session over HTTP.
            // We mint one eagerly because StreamableHttp requires both REST and SSE legs to
            // agree on the same identifier.
            streamableSessions.createSession(serverFactory).also {
                call.response.headers.append(MCP_SESSION_ID_HEADER, it.id)
            }
        } else {
            val existing = streamableSessions.getSession(sessionIdHeader)
            if (existing == null) {
                call.respond(HttpStatusCode.Gone, "Unknown MCP session")
                return
            }
            call.response.headers.append(MCP_SESSION_ID_HEADER, existing.id)
            existing
        }

        when (val outcome = runCatching { session.mcpTransport.processMessage(message) }.getOrElse { throwable ->
            if (throwable is TimeoutCancellationException) {
                call.respond(HttpStatusCode.RequestTimeout, "MCP request timed out")
            } else {
                logger.error("Error handling MCP request for session ${session.id}", throwable)
                call.respond(HttpStatusCode.InternalServerError, "MCP handling error")
            }
            return
        }) {
            is StreamableHttpResponse.Json -> {
                val payload = McpJson.encodeToString(outcome.message)
                call.respondText(payload, ContentType.Application.Json)
            }

            StreamableHttpResponse.Accepted -> {
                // Indicates the server deferred the response and will stream data through SSE.
                call.respond(HttpStatusCode.Accepted)
            }
        }
    }

    /**
     * Handles explicit session teardown: validates the client-provided session id, closes the
     * transport + MCP server pair, and reports whether a matching session still existed.
     */
    suspend fun handleStreamableDelete(call: ApplicationCall) {
        val sessionId = call.request.headers[MCP_SESSION_ID_HEADER]
        if (sessionId.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Missing MCP session header")
            return
        }

        val closed = streamableSessions.closeSession(sessionId)
        if (!closed) {
            call.respond(HttpStatusCode.Gone, "Session already closed")
            return
        }

        call.respond(HttpStatusCode.NoContent)
    }


    companion object {
        private val logger = LoggerFactory.getLogger(McpStreamableHttpBridge::class.java)
        private const val MCP_SESSION_ID_HEADER = "mcp-session-id"
        private const val LAST_EVENT_ID_HEADER = "Last-Event-ID"
    }
}
