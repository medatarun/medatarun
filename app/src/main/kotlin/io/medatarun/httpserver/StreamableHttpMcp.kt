package io.medatarun.app.io.medatarun.httpserver

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.sse.ServerSSESession
import io.medatarun.httpserver.StreamableHttpResponse
import io.medatarun.httpserver.StreamableHttpSessionManager
import io.modelcontextprotocol.kotlin.sdk.JSONRPCMessage
import io.modelcontextprotocol.kotlin.sdk.JSONRPCRequest
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.shared.McpJson
import kotlinx.coroutines.TimeoutCancellationException
import org.slf4j.LoggerFactory
import kotlin.text.isNullOrBlank
import kotlin.text.toLongOrNull

class StreamableHttpMcp(serverFactory: () -> Server) {

    private val streamableSessions = StreamableHttpSessionManager(serverFactory)


    suspend fun handleStreamableSse(session: ServerSSESession) {

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

        runCatching {
            streamSession.transport.attachSse(session, lastEventId)
        }.onFailure { throwable ->
            logger.warn("SSE stream failure for session $sessionId", throwable)
        }
    }


    suspend fun handleStreamablePost(call: ApplicationCall) {
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

            streamableSessions.createSession().also {
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

        when (val outcome = runCatching { session.transport.processMessage(message) }.getOrElse { throwable ->
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
                call.respond(HttpStatusCode.Accepted)
            }
        }
    }

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
        private val logger = LoggerFactory.getLogger(StreamableHttpMcp::class.java)
        private const val MCP_SESSION_ID_HEADER = "mcp-session-id"
        private const val LAST_EVENT_ID_HEADER = "Last-Event-ID"
    }
}