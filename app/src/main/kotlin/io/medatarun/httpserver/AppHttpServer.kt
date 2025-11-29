package io.medatarun.httpserver

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.medatarun.httpserver.mcp.McpServerBuilder
import io.medatarun.httpserver.mcp.McpStreamableHttpBridge
import io.medatarun.httpserver.rest.RestApiDoc
import io.medatarun.httpserver.rest.RestCommandInvocation
import io.medatarun.httpserver.ui.UI
import io.medatarun.model.model.EntityDefId
import io.medatarun.model.model.ModelId
import io.medatarun.resources.AppResources
import io.medatarun.resources.ResourceRepository
import io.medatarun.runtime.AppRuntime
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import org.slf4j.LoggerFactory
import java.util.Locale

/**
 * REST API server that mirrors the CLI reflection behaviour on top of Ktor.
 */
class AppHttpServer(
    private val runtime: AppRuntime,
    private val enableMcp: Boolean = true,
    private val enableHealth: Boolean = true,
    private val enableApi: Boolean = true
) {
    private val logger = LoggerFactory.getLogger(AppHttpServer::class.java)
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

        install(StatusPages) {
            status(HttpStatusCode.NotFound) { call, status ->
                val path = call.request.path()

                // Be careful to not replace 404 coming from API, MCP, SSE or files
                if (
                    path.startsWith("/api") ||
                    path.startsWith("/mcp") ||
                    path.startsWith("/sse") ||
                    path.startsWith("/ui") ||
                    path.startsWith("/assets") ||
                    path.contains('.')
                ) {
                    call.respond(status)
                    return@status
                }

                // Fallback React Router : servir index.html
                val index = javaClass.classLoader.getResource("static/index.html")
                if (index != null)
                    call.respondBytes(index.readBytes(), ContentType.Text.Html)
                else
                    call.respond(status)
            }
            exception<Throwable> { call, cause ->
                call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
            }
        }


        routing {
            staticResources("/", "static") {
                default("index.html")
            }
            if (enableHealth) {
                get("/health") {
                    call.respond(mapOf("status" to "ok"))
                }
            }

            if (enableApi) {

                get("/api") {
                    call.respond(restApiDoc.buildApiDescription())
                }

                route("/api/{resource}/{function}") {
                    get { restCommandInvocation.processInvocation(call) }
                    post { restCommandInvocation.processInvocation(call) }
                }
            }

            get("/ui/api/models") {
                call.respondText(UI(runtime).modelListJson(detectLocale(call)), ContentType.Application.Json)
            }
            get("/ui/api/models/{modelId}") {
                val modelId = call.parameters["modelId"] ?: throw NotFoundException()
                call.respondText(UI(runtime).modelJson(ModelId(modelId), detectLocale(call)), ContentType.Application.Json)
            }
            get("/ui/api/models/{modelId}/entitydefs/{entityDefId}") {
                val modelId = call.parameters["modelId"] ?: throw NotFoundException()
                val entityDefId = call.parameters["entityDefId"] ?: throw NotFoundException()
                call.respondText(UI(runtime).entityDefJson(ModelId(modelId), EntityDefId(entityDefId), detectLocale(call)), ContentType.Application.Json)
            }

            if (enableMcp) {
                route("/sse") {
                    mcp {
                        return@mcp mcpServerBuilder.buildMcpServer()
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


}

private fun detectLocale(call: ApplicationCall): Locale {
    val header = call.request.headers["Accept-Language"]
    val firstTag = header
        ?.split(",")
        ?.map { it.substringBefore(";").trim() }
        ?.firstOrNull { it.isNotEmpty() }

    return firstTag?.let { Locale.forLanguageTag(it) } ?: Locale.getDefault()
}