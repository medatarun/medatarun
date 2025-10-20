package io.medatarun.httpserver.mcp

import io.ktor.server.sse.*
import io.modelcontextprotocol.kotlin.sdk.JSONRPCMessage
import io.modelcontextprotocol.kotlin.sdk.JSONRPCRequest
import io.modelcontextprotocol.kotlin.sdk.JSONRPCResponse
import io.modelcontextprotocol.kotlin.sdk.RequestId
import io.modelcontextprotocol.kotlin.sdk.shared.AbstractTransport
import io.modelcontextprotocol.kotlin.sdk.shared.McpJson
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.seconds


/**
 * StreamableHTTP transport used by the MCP server for a single session.
 *
 * Responsibilities:
 * - Acts as the bridge between the MCP `Server` and the HTTP layer: incoming HTTP POST payloads
 *   are decoded and passed to `_onMessage`, while outbound MCP messages are fanned out to SSE
 *   clients or stored as pending JSON-RPC responses.
 * - Tracks in-flight request/response pairs via `pendingResponses` so synchronous POST callers
 *   get their result (or timeout) while still supporting async notifications.
 * - Buffers outbound events (`events`) to replay them when a client reconnects with
 *   `Last-Event-ID`, keeping long-lived streams consistent.
 * - Manages the set of SSE connections (`connections`), replaying on attach and detaching when
 *   the coroutine completes or transmission fails.
 * - Provides session-scoped teardown: `close()` cancels pending work, closes SSE channels, and
 *   notifies the MCP server through `_onClose()` to reclaim resources cleanly.
 *
 * Implementation mirrors the official TypeScript StreamableHTTP transport but uses Kotlin
 * concurrency primitives (coroutines, Mutex, CompletableDeferred) instead of Promises.
 */
internal class McpStreamableHttpTransport(
    val sessionId: String,
) : AbstractTransport() {

    private val mutex = Mutex()
    private val events = ArrayDeque<StreamableEvent>()
    private val connections = mutableSetOf<ServerSSESession>()
    private val pendingResponses = mutableMapOf<RequestId, CompletableDeferred<JSONRPCMessage>>()

    private var nextEventId = 1L
    private var closed = false

    override suspend fun start() {
        // Nothing to eagerly start for streamable HTTP
        logger.debug("Streamable transport [$sessionId] starting")
    }

    override suspend fun send(message: JSONRPCMessage) {
        when (message) {
            is JSONRPCResponse -> deliverResponse(message)
            else -> deliverEvent(message)
        }
    }

    override suspend fun close() {
        var toClose: List<ServerSSESession> = emptyList()
        var pending: List<CompletableDeferred<JSONRPCMessage>> = emptyList()

        mutex.withLock {
            if (closed) {
                return
            }
            closed = true
            toClose = connections.toList()
            pending = pendingResponses.values.toList()
            connections.clear()
            pendingResponses.clear()
        }

        pending.forEach { it.cancel() }
        toClose.forEach { session ->
            runCatching { session.close() }
        }

        logger.debug("Streamable transport [$sessionId] closed")
        _onClose()
    }

    suspend fun processMessage(message: JSONRPCMessage, timeoutSeconds: Long = 30): StreamableHttpResponse {
        return when (message) {
            is JSONRPCRequest -> {
                val deferred = CompletableDeferred<JSONRPCMessage>()
                registerPending(message.id, deferred)
                try {
                    _onMessage(message)
                } catch (throwable: Throwable) {
                    unregisterPending(message.id)
                    throw throwable
                }

                val response = try {
                    withTimeout(timeoutSeconds.seconds) {
                        deferred.await()
                    }
                } catch (timeout: TimeoutCancellationException) {
                    unregisterPending(message.id)
                    logger.warn("Timeout while waiting response for request ${message.id} on session $sessionId")
                    throw timeout
                }

                StreamableHttpResponse.Json(response)
            }

            else -> {
                _onMessage(message)
                StreamableHttpResponse.Accepted
            }
        }
    }

    suspend fun attachSse(session: ServerSSESession, lastEventId: Long?) {
        val replayEvents = mutex.withLock {
            if (closed) error("Session $sessionId is closed")
            connections += session
            events.filter { it.id > (lastEventId ?: 0L) }
        }

        // Replay previous messages if client reconnects.
        for (event in replayEvents) {
            runCatching {
                session.send(
                    event = "message",
                    data = event.payload,
                    id = event.id.toString(),
                )
            }.onFailure { throwable ->
                logger.warn("Failed to replay event ${event.id} for session $sessionId", throwable)
                detachConnection(session)
                throw throwable
            }
        }

        try {
            session.coroutineContext.job.join()
        } finally {
            detachConnection(session)
        }
    }

    private suspend fun deliverResponse(response: JSONRPCResponse) {
        val deferred = unregisterPending(response.id)
        if (deferred != null) {
            deferred.complete(response)
            return
        }

        // Unexpected response, forward as event so client can decide what to do.
        deliverEvent(response)
    }

    private suspend fun deliverEvent(message: JSONRPCMessage) {
        val payload = McpJson.encodeToString(message)
        val event = mutex.withLock {
            val event = StreamableEvent(nextEventId++, payload)
            events.addLast(event)
            if (events.size > MAX_STORED_EVENTS) {
                events.removeFirst()
            }
            event
        }

        val failed = mutableListOf<ServerSSESession>()
        mutex.withLock { connections.toList() }.forEach { session ->
            runCatching {
                session.send(
                    event = "message",
                    data = event.payload,
                    id = event.id.toString(),
                )
            }.onFailure {
                logger.warn("Failed to dispatch event ${event.id} to session $sessionId", it)
                failed += session
            }
        }

        if (failed.isNotEmpty()) {
            mutex.withLock {
                connections.removeAll(failed.toSet())
            }
        }
    }

    private suspend fun detachConnection(session: ServerSSESession) {
        mutex.withLock {
            connections.remove(session)
        }
    }

    private suspend fun registerPending(
        id: RequestId,
        deferred: CompletableDeferred<JSONRPCMessage>,
    ) {
        mutex.withLock {
            pendingResponses[id] = deferred
        }
    }

    private suspend fun unregisterPending(id: RequestId): CompletableDeferred<JSONRPCMessage>? {
        return mutex.withLock {
            pendingResponses.remove(id)
        }
    }

    companion object {

        private val logger = LoggerFactory.getLogger("StreamableHttpTransport")

        private const val MAX_STORED_EVENTS = 1_000

    }
}
