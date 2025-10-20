package io.medatarun.httpserver

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.medatarun.app.io.medatarun.httpserver.mcp.McpServerBuilder
import io.medatarun.cli.AppCLIResources
import io.medatarun.httpserver.mcp.McpStreamableHttpBridge
import io.medatarun.httpserver.rest.RestApiDoc
import io.medatarun.httpserver.rest.RestCommandInvocation
import io.medatarun.resources.ResourceRepository
import io.medatarun.runtime.AppRuntime
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import org.slf4j.LoggerFactory

/**
 * REST API server that mirrors the CLI reflection behaviour on top of Ktor.
 */
class RestApi(
    private val runtime: AppRuntime,
) {
    private val logger = LoggerFactory.getLogger(RestApi::class.java)
    private val resources = AppCLIResources(runtime)
    private val resourceRepository = ResourceRepository(resources)
    private val mcpServerBuilder = McpServerBuilder(resourceRepository)
    private val restApiDoc = RestApiDoc(resourceRepository)
    private val restCommandInvocation = RestCommandInvocation(resourceRepository)


    @Volatile
    private var engine: EmbeddedServer<*, *>? = null

    /**
     * Starts the REST API server. Subsequent calls while the server is running throw an [IllegalStateException].
     */
    fun start(
        host: String = "0.0.0.0",
        port: Int = 8080,
        wait: Boolean = false,
    ) {
        synchronized(this) {
            check(engine == null) { "RestApi server already running" }
            engine = embeddedServer(Netty, host = host, port = port, module = { configure() }).also {
                logger.info("Starting REST API on http://$host:$port")
                it.start(wait = wait)
            }
        }
    }

    /**
     * Stops the REST API server if it is running.
     */
    fun stop(gracePeriodMillis: Long = 1_000, timeoutMillis: Long = 2_000) {
        synchronized(this) {
            engine?.let {
                logger.info("Stopping REST API")
                it.stop(gracePeriodMillis, timeoutMillis)
            }
            engine = null
        }
    }

    private fun Application.configure() {

        val mcpStreamableHttpBridge = McpStreamableHttpBridge(serverFactory = mcpServerBuilder::buildMcpServer)

        install(ContentNegotiation) { json() }
        install(SSE)


        routing {
            get("/health") {
                call.respond(mapOf("status" to "ok"))
            }

            get("/api") {
                call.respond(restApiDoc.buildApiDescription())
            }

            route("/api/{resource}/{function}") {
                get { restCommandInvocation.processInvocation(call) }
                post { restCommandInvocation.processInvocation(call) }
            }

            mcp {
                return@mcp mcpServerBuilder.buildMcpServer()
            }

            route("/mcp") {
                post { mcpStreamableHttpBridge.handleStreamablePost(call) }
                delete { mcpStreamableHttpBridge.handleStreamableDelete(call) }
                sse {
                    mcpStreamableHttpBridge.handleStreamableSse(this)
                }
            }
        }
    }



}
