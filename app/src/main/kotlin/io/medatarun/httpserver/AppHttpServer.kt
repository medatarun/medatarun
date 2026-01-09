package io.medatarun.httpserver

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.medatarun.actions.runtime.ActionCtxFactory
import io.medatarun.actions.runtime.ActionRegistry
import io.medatarun.auth.ports.exposed.AuthEmbeddedOIDCService
import io.medatarun.auth.ports.exposed.AuthEmbeddedUserService
import io.medatarun.httpserver.cli.installCLI
import io.medatarun.httpserver.commons.installCors
import io.medatarun.httpserver.commons.installHealth
import io.medatarun.httpserver.commons.installJwtSecurity
import io.medatarun.httpserver.mcp.McpServerBuilder
import io.medatarun.httpserver.mcp.installMcp
import io.medatarun.httpserver.oidc.installOidc
import io.medatarun.httpserver.rest.RestApiDoc
import io.medatarun.httpserver.rest.RestCommandInvocation
import io.medatarun.httpserver.rest.installActionsApi
import io.medatarun.httpserver.ui.*
import io.medatarun.kernel.getService
import io.medatarun.runtime.AppRuntime
import io.metadatarun.ext.config.actions.ConfigAgentInstructions
import org.slf4j.LoggerFactory
import java.net.URI

/**
 * Main application Http server built with Ktor that serves:
 *
 * - an MCP server
 * - a User Interface: accessible at http(s)://<host> or http(s)://<host>/ui
 * - a Rest API: accessible to http(s)://<host>/api
 * - a health endpoint:  https://<host>/health
 * - OIDC endpoints
 */
class AppHttpServer(
    private val runtime: AppRuntime,
    private val baseUri: URI,

    private val enableMcpSse: Boolean = false,
    private val enableMcpStreamingHttp: Boolean = true,
    private val enableHealth: Boolean = true,
    private val enableApi: Boolean = true,

    ) {
    private val logger = LoggerFactory.getLogger(AppHttpServer::class.java)
    private val actionRegistry = ActionRegistry(runtime.extensionRegistry)
    private val actionCtxFactory = ActionCtxFactory(runtime, actionRegistry, runtime.services)
    private val mcpServerBuilder = McpServerBuilder(
        actionRegistry,
        configAgentInstructions = ConfigAgentInstructions(),
        actionCtxFactory = actionCtxFactory
    )
    private val restApiDoc = RestApiDoc(actionRegistry)
    private val restCommandInvocation = RestCommandInvocation(actionRegistry, actionCtxFactory)

    private val uiIndexTemplate = UIIndexTemplate()

    val userService = runtime.services.getService<AuthEmbeddedUserService>()
    val oidcService = runtime.services.getService<AuthEmbeddedOIDCService>()

    val bootstrap = userService.loadOrCreateBootstrapSecret { secret ->
        logger.warn("----------------------------------------------------------")
        logger.warn("⚠️ This message disappear once the secret is used.")
        logger.warn("")
        logger.warn("BOOTSTRAP SECRET (one-time usage): $secret")
        logger.warn("")
        logger.warn("Use it to create your admin account with CLI")
        logger.warn("")
        logger.warn("medatarun auth admin_bootstrap --username=your_admin_name --fullname=\"your name\" --password=your_password --bootstrap=$secret")
        logger.warn("")
        logger.warn("or with API")
        logger.warn("")
        logger.warn("curl http://<host>:<port>/api/auth/admin_bootstrap -H \"Content-Type: application/json\" -d '{\"secret\":\"$secret\",\"fullname\":\"Admin\",\"username\":\"your_admin_name\",\"password\":\"your_password\"}'")
        logger.warn("----------------------------------------------------------")
    }

    @Volatile
    private var engine: EmbeddedServer<*, *>? = null

    /**
     * Starts the REST API server. Subsequent calls while the server is running throw an [IllegalStateException].
     */
    fun start(
        host: String,
        port: Int,
        wait: Boolean,
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


        install(ContentNegotiation) { json() }
        install(SSE)
        installCors()
        installUIStatusPageAndSpaFallback(uiIndexTemplate, listOf("/api", "/mcp", "/sse", "/oidc"))
        installJwtSecurity(oidcService)

        routing {

            installUIStaticResources()
            installUIHomepage(uiIndexTemplate)
            installUIApis(runtime, actionRegistry)

            installActionsApi(restApiDoc, restCommandInvocation)

            installCLI(actionRegistry)

            installOidc(oidcService, userService, baseUri)

            installMcp(mcpServerBuilder)

            installHealth()

        }
    }
}

