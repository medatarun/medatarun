package io.medatarun.httpserver

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.medatarun.actions.runtime.*
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
    private val publicBaseUrl: URI,
    private val services: AppHttpServerServices
) {

    private val mcpServerBuilder = McpServerBuilder(
        actionRegistry = services.actionRegistry,
        configAgentInstructions = ConfigAgentInstructions(),
        actionCtxFactory = services.actionCtxFactory,
        actionInvoker = services.actionInvoker,
    )
    private val restApiDoc = RestApiDoc(services.actionRegistry)
    private val restCommandInvocation = RestCommandInvocation(services.actionInvoker, services.actionCtxFactory)

    private val uiIndexTemplate = UIIndexTemplate()


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
            engine = embeddedServer(
                factory = Netty,
                host = host,
                port = port,
                module = {
                    configure()
                    monitor.subscribe(ServerReady) {
                        // Important, this displays the boostrap admin secret at startup when not already consumed
                        bootstrapMessage()
                        @Suppress("HttpUrlsUsage")
                        logger.info("Starting REST API on http://$host:$port with publicBaseUrl=$publicBaseUrl")
                    }
                }
            ).also { it.start(wait = wait) }
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

        val oidcAuthority = services.oidcService.oidcAuthority(publicBaseUrl)
        val oidcClientId = services.oidcService.oidcClientId()

        install(ContentNegotiation) { json() }
        install(SSE)
        installCors()
        installUIStatusPageAndSpaFallback(
            uiIndexTemplate,
            listOf("/api", "/mcp", "/sse", "/oidc"),
            oidcAuthority,
            oidcClientId
        )
        installJwtSecurity(services.oidcService)

        routing {

            installUIStaticResources()
            installUIHomepage(uiIndexTemplate, oidcAuthority, oidcClientId)
            installUIApis(services.runtime, services.actionRegistry)

            installActionsApi(restApiDoc, restCommandInvocation, services.principalFactory)

            installCLI(services.actionRegistry)

            installOidc(services.oidcService, services.userService, publicBaseUrl)


            installMcp(mcpServerBuilder, principalFactory = services.principalFactory)


            installHealth()

        }
    }

    fun bootstrapMessage() {
        services.userService.loadOrCreateBootstrapSecret { secret ->
            logger.warn("----------------------------------------------------------")
            logger.warn("⚠️ This message disappear once the secret is used.")
            logger.warn("")
            logger.warn("BOOTSTRAP SECRET (one-time usage): $secret")
            logger.warn("")
            logger.warn("Use it to create your admin account with CLI")
            logger.warn("")
            logger.warn("medatarun auth admin_bootstrap --username=your_admin_name --fullname=\"your name\" --password=your_password --secret=$secret")
            logger.warn("")
            logger.warn("or with API")
            logger.warn("")
            logger.warn("curl $publicBaseUrl/api/auth/admin_bootstrap -H \"Content-Type: application/json\" -d '{\"secret\":\"$secret\",\"fullname\":\"Admin\",\"username\":\"your_admin_name\",\"password\":\"your_password\"}'")
            logger.warn("----------------------------------------------------------")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AppHttpServer::class.java)
    }
}
