package io.medatarun.httpserver

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.actions.runtime.*
import io.medatarun.auth.ports.exposed.ActorService
import io.medatarun.auth.ports.exposed.OidcService
import io.medatarun.auth.ports.exposed.UserService
import io.medatarun.httpserver.cli.installCLI
import io.medatarun.httpserver.commons.AppHttpServerJwtSecurity.AUTH_MEDATARUN_JWT
import io.medatarun.httpserver.commons.AppPrincipalFactory
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
import io.medatarun.security.SecurityRulesProvider
import io.medatarun.types.TypeDescriptor
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
    private val publicBaseUrl: URI,
) {

    private val actionSecurityRuleEvaluators = ActionSecurityRuleEvaluators(
        runtime.extensionRegistry.findContributionsFlat(SecurityRulesProvider::class)
            .flatMap { it.getRules() }
    )
    private val actionTypesRegistry = ActionTypesRegistry(
        runtime.extensionRegistry.findContributionsFlat(TypeDescriptor::class)
    )
    private val actionRegistry = ActionRegistry(
        actionSecurityRuleEvaluators,
        actionTypesRegistry,
        runtime.extensionRegistry.findContributionsFlat(ActionProvider::class)
    )

    private val actionInvoker = ActionInvoker(
        actionRegistry,
        actionTypesRegistry,
        actionSecurityRuleEvaluators
    )

    private val actionCtxFactory = ActionCtxFactory(runtime, actionInvoker, runtime.services)

    private val mcpServerBuilder = McpServerBuilder(
        actionRegistry = actionRegistry,
        configAgentInstructions = ConfigAgentInstructions(),
        actionCtxFactory = actionCtxFactory,
        actionInvoker = actionInvoker,
    )
    private val restApiDoc = RestApiDoc(actionRegistry)
    private val restCommandInvocation = RestCommandInvocation(actionInvoker, actionCtxFactory)

    private val uiIndexTemplate = UIIndexTemplate()

    val userService = runtime.services.getService<UserService>()
    val oidcService = runtime.services.getService<OidcService>()
    val actorService = runtime.services.getService<ActorService>()
    private val principalFactory = AppPrincipalFactory(actorService)


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
            engine = embeddedServer(Netty, host = host, port = port, module = { configure() })
                .also {
                    @Suppress("HttpUrlsUsage")
                    logger.info("Starting REST API on http://$host:$port with publicBaseUrl=$publicBaseUrl")
                    // Important, this displays the boostrap admin secret at startup when not already consumed
                    bootstrapMessage()
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

        val oidcAuthority = oidcService.oidcAuthority(publicBaseUrl)
        val oidcClientId = oidcService.oidcClientId()

        install(ContentNegotiation) { json() }
        install(SSE)
        installCors()
        installUIStatusPageAndSpaFallback(uiIndexTemplate, listOf("/api", "/mcp", "/sse", "/oidc"), oidcAuthority, oidcClientId)
        installJwtSecurity(oidcService)

        routing {

            installUIStaticResources()
            installUIHomepage(uiIndexTemplate, oidcAuthority, oidcClientId)
            installUIApis(runtime, actionRegistry)

            installActionsApi(restApiDoc, restCommandInvocation, principalFactory)

            installCLI(actionRegistry)

            installOidc(oidcService, userService, publicBaseUrl)

            authenticate(AUTH_MEDATARUN_JWT) {
                installMcp(mcpServerBuilder, principalFactory = principalFactory)
            }

            installHealth()

        }
    }

    fun bootstrapMessage() {
        userService.loadOrCreateBootstrapSecret { secret ->
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
