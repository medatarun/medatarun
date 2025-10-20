package io.medatarun.httpserver

import io.ktor.server.sse.ServerSSESession
import io.modelcontextprotocol.kotlin.sdk.JSONRPCMessage
import io.modelcontextprotocol.kotlin.sdk.JSONRPCRequest
import io.modelcontextprotocol.kotlin.sdk.JSONRPCResponse
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.shared.McpJson
import io.modelcontextprotocol.kotlin.sdk.RequestId
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.job
import org.slf4j.LoggerFactory
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import io.modelcontextprotocol.kotlin.sdk.shared.AbstractTransport

private val logger = LoggerFactory.getLogger("StreamableHttpTransport")

private const val MAX_STORED_EVENTS = 1_000

internal sealed interface StreamableHttpResponse {
    data class Json(val message: JSONRPCMessage) : StreamableHttpResponse
    data object Accepted : StreamableHttpResponse
}

private data class StreamableEvent(val id: Long, val payload: String)

internal class StreamableHttpTransport(
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
}

internal class StreamableHttpSession(
    val id: String,
    private val server: Server,
    val transport: StreamableHttpTransport,
) {
    suspend fun close() {
        try {
            server.close()
        } catch (_: Throwable) {
        }

        try {
            transport.close()
        } catch (_: Throwable) {
        }
    }
}

internal class StreamableHttpSessionManager(
    private val serverFactory: () -> Server,
) {
    private val sessions = ConcurrentHashMap<String, StreamableHttpSession>()

    suspend fun createSession(): StreamableHttpSession {
        val sessionId = UUID.randomUUID().toString()
        val transport = StreamableHttpTransport(sessionId)
        val server = serverFactory()
        server.connect(transport)

        val session = StreamableHttpSession(sessionId, server, transport)
        transport.onClose { sessions.remove(sessionId) }
        server.onClose { sessions.remove(sessionId) }

        sessions[sessionId] = session
        return session
    }

    fun getSession(id: String): StreamableHttpSession? = sessions[id]

    suspend fun closeSession(id: String): Boolean {
        val removed = sessions.remove(id)
        removed?.close()
        return removed != null
    }
}
