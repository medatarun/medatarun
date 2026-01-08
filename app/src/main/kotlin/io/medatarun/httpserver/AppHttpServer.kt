package io.medatarun.httpserver

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.medatarun.actions.ports.needs.MedatarunPrincipal
import io.medatarun.actions.runtime.ActionCtxFactory
import io.medatarun.actions.runtime.ActionRegistry
import io.medatarun.auth.ports.exposed.AuthEmbeddedService
import io.medatarun.httpserver.cli.CliActionRegistry
import io.medatarun.httpserver.mcp.McpServerBuilder
import io.medatarun.httpserver.mcp.McpStreamableHttpBridge
import io.medatarun.httpserver.rest.RestApiDoc
import io.medatarun.httpserver.rest.RestCommandInvocation
import io.medatarun.httpserver.ui.UI
import io.medatarun.kernel.getService
import io.medatarun.model.domain.ModelKey
import io.medatarun.runtime.AppRuntime
import io.metadatarun.ext.config.actions.ConfigAgentInstructions
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*

/**
 * Main application Http server built with Ktor that serves:
 *
 * - an MCP server with SSE (disabled by default, because of bugs in kotlin's official MCP SDK namely https://github.com/modelcontextprotocol/kotlin-sdk/issues/237)
 * - an MCP server with Streamable Http transport (built on top of the MCP SDK) as
 *   expected by modern AI agents (Codex, Claude Code, etc.). This had been built for this project as the official
 *   SDK can not provide it yet.
 * - a User Interface: accessible at http(s)://<host> or http(s)://<host>/ui
 * - a Rest API: accessible to http(s)://<host>/api
 * - a health endpoint:  https://<host>/health
 */
class AppHttpServer(
    private val runtime: AppRuntime,

    private val enableMcpSse: Boolean = false,
    private val enableMcpStreamingHttp: Boolean = true,
    private val enableHealth: Boolean = true,
    private val enableApi: Boolean = true
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


    val authEmbeddedService = runtime.services.getService<AuthEmbeddedService>()

    val bootstrap = authEmbeddedService.loadOrCreateBootstrapSecret { secret ->
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

        val mcpStreamableHttpBridge = McpStreamableHttpBridge()

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
        install(Authentication) {
            jwt(AUTH_MEDATARUN_JWT) {
                skipWhen { call ->
                    call.request.headers[HttpHeaders.Authorization] == null
                }
                verifier(
                    JWT.require(Algorithm.RSA256(authEmbeddedService.oidcPublicKey(), null))
                        .withIssuer(authEmbeddedService.oidcIssuer())
                        .withAudience(authEmbeddedService.oidcAudience())
                        .build()
                )
                validate { cred ->
                    JWTPrincipal(cred.payload)
                }
                challenge { _, _ ->
                    call.respond(HttpStatusCode.Unauthorized, "invalid or missing token")
                }
            }
        }

        routing {
            // Authentication: all public
            staticResources("/", "static") {
                default("index.html")
            }

            // Authentication: all public -> otherwise UI can not load
            get("/health") {
                call.respond(mapOf("status" to "ok"))
            }

            // Authentication: all public -> Jwks must be public for discovert
            get(authEmbeddedService.oidcJwksUri()) {
                call.respond(authEmbeddedService.oidcJwks())
            }

            get("/api") {
                // Authentication: all public -> everybody needs to know API description
                call.respond(restApiDoc.buildApiDescription())
            }

            authenticate(AUTH_MEDATARUN_JWT) {
                route("/api/{actionGroupKey}/{actionKey}") {
                    // Authentication: token required but not always, the action will check that principal
                    // is present with correct roles the action require a principal, and not all actions need one
                    // So we don't block actions if principal is missing
                    get { restCommandInvocation.processInvocation(call, toMedatarunPrincipal(call)) }
                    post { restCommandInvocation.processInvocation(call, toMedatarunPrincipal(call)) }
                }
            }
            get("/cli/api/action-registry") {
                // Authentication: actino registry for CLI is public (otherwise no help on CLI)
                call.respond(CliActionRegistry(actionRegistry).actionRegistryDto())
            }
            get("/ui/api/action-registry") {
                // Authentication: action registry for UI is public (otherwise no help on UI)
                call.respond(UI(runtime, actionRegistry).actionRegistryDto(detectLocale(call)))
            }

            authenticate(AUTH_MEDATARUN_JWT) {
                // Authentication: required
                get("/ui/api/models") {

                    call.respondText(
                        UI(runtime, actionRegistry).modelListJson(detectLocale(call)),
                        ContentType.Application.Json
                    )

                }
            }

            authenticate(AUTH_MEDATARUN_JWT) {
                get("/ui/api/models/{modelId}") {

                    val modelId = call.parameters["modelId"] ?: throw NotFoundException()
                    call.respondText(
                        UI(runtime, actionRegistry).modelJson(ModelKey(modelId), detectLocale(call)),
                        ContentType.Application.Json
                    )

                }
            }

            // ----------------------------------------------------------------
            // MCP server
            // ----------------------------------------------------------------

            if (enableMcpSse) {
                // SSE protocol, buggy in Kotlin, waiting for fix, so disabled
                // Authentication: some tools will required, some others not,
                // we let the tool building and actions decide

                route("/sse") {
                    mcp {
                        val user = toMedatarunPrincipal(call)
                        return@mcp mcpServerBuilder.buildMcpServer(user)
                    }
                }
            }

            if (enableMcpStreamingHttp) {
                route("/mcp") {
                    post {
                        val principal = toMedatarunPrincipal(call)
                        mcpStreamableHttpBridge.handleStreamablePost(call) {
                            mcpServerBuilder.buildMcpServer(principal)
                        }
                    }
                    delete {
                        mcpStreamableHttpBridge.handleStreamableDelete(call)
                    }
                    sse {
                        mcpStreamableHttpBridge.handleStreamableSse(this)
                    }
                }
            }


        }

    }


    private fun toMedatarunPrincipal(call: ApplicationCall): MedatarunPrincipal? {
        val principal = call.authentication.principal<JWTPrincipal>() ?: return null
        val principalIssuer = principal.issuer ?: return null
        val principalSubject = principal.subject ?: return null
        val principalAdmin = principal.getClaim("role", String::class) == "admin"
        return object : MedatarunPrincipal {
            override val sub: String = principalSubject
            override val issuer: String = principalIssuer
            override val isAdmin: Boolean = principalAdmin
            override val issuedAt: Instant? = principal.issuedAt?.toInstant()
            override val expiresAt: Instant? = principal.expiresAt?.toInstant()
            override val audience: List<String> = principal.audience
            override val claims =
                principal.payload.claims?.map { it.key to it.value?.asString() }?.toMap() ?: emptyMap()

        }
    }

    companion object {
        const val AUTH_MEDATARUN_JWT = "medatarun-jwt"
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

