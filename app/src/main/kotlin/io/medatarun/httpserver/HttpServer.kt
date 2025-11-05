package io.medatarun.httpserver

import io.ktor.http.ContentType
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.medatarun.httpserver.mcp.McpServerBuilder
import io.medatarun.httpserver.mcp.McpStreamableHttpBridge
import io.medatarun.httpserver.rest.RestApiDoc
import io.medatarun.httpserver.rest.RestCommandInvocation
import io.medatarun.httpserver.ui.Links
import io.medatarun.resources.AppResources
import io.medatarun.resources.ResourceRepository
import io.medatarun.runtime.AppRuntime
import io.medatarun.httpserver.ui.UI
import io.medatarun.model.model.EntityDefId
import io.medatarun.model.model.ModelId
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import org.slf4j.LoggerFactory

/**
 * REST API server that mirrors the CLI reflection behaviour on top of Ktor.
 */
class RestApi(
    private val runtime: AppRuntime,
) {
    private val logger = LoggerFactory.getLogger(RestApi::class.java)
    private val resources = AppResources(runtime)
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

            route("/sse") {
                mcp {
                    return@mcp mcpServerBuilder.buildMcpServer()
                }
            }

            get("/ui") {
                headers { put { "Content-Type" to "text/html" } }
                call.respondText(UI(runtime).renderModelList(), ContentType.Text.Html)
            }
            get(Links.toModel(null)) {
                val id = call.pathParameters["id"] ?: throw NotFoundException("")
                call.respondText(UI(runtime).renderModel(ModelId(id)), ContentType.Text.Html)
            }
            get(Links.toEntityDef(null, null)) {
                val modelId = call.pathParameters["modelId"] ?: throw NotFoundException("")
                val entityDefId = call.pathParameters["entityDefId"] ?: throw NotFoundException("")
                call.respondText(UI(runtime).renderEntityDef(ModelId(modelId), EntityDefId(entityDefId)), ContentType.Text.Html)
            }
            get(Links.toCommands()) {
                call.respondText(UI(runtime).commands(null, null), ContentType.Text.Html)
            }
            post(Links.toCommands()) {
                try {
                    val params =
                        call.receiveParameters()["action"] ?: throw BadRequestException("No action form parameter found")
                    call.respondText(UI(runtime).commands(params, null), ContentType.Text.Html)
                } catch (err: Throwable) {
                    call.respondText(UI(runtime).commands(null, err.message), ContentType.Text.Html)
                }

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
