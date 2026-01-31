package io.medatarun.httpserver.mcp

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.medatarun.httpserver.commons.AppHttpServerJwtSecurity.AUTH_MEDATARUN_JWT
import io.medatarun.httpserver.commons.AppPrincipalFactory
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import org.slf4j.LoggerFactory

/**
 * Install routes for MCP (Model Context Protocol) for AIs
 *
 * Note that the SSE route is disabled, because it is buggy as hell
 * because of bugs in kotlin's official MCP SDK namely https://github.com/modelcontextprotocol/kotlin-sdk/issues/237
 *
 * So we rely only on Streamable Http transport (built on top of the MCP SDK) as expected by modern AI agents (Codex, Claude Code, etc.).
 * This transport had been been built for this project, because the official
 * SDK can not provide it yet.
 *
 */
fun Routing.installMcp(
    mcpServerBuilder: McpServerBuilder,
    principalFactory: AppPrincipalFactory,
    enableMcpSse: Boolean = false,
    enableMcpStreamingHttp: Boolean = true
) {
    val logger = LoggerFactory.getLogger("MCP")

    fun debugCall(call: ApplicationCall) {
        val method = call.request.httpMethod.value
        val path = call.request.path()
        val principal = call.authentication.principal<JWTPrincipal>()
        val headers = call.request.headers.names().map { it to call.request.headers[it] }.toMap()
        logger.debug("path=$path method=$method principal=$principal headers=$headers")
    }

    val mcpStreamableHttpBridge = McpStreamableHttpBridge()

    // ----------------------------------------------------------------
    // MCP server
    // ----------------------------------------------------------------

    if (enableMcpSse) {
        // SSE protocol, buggy in Kotlin, waiting for fix, so disabled
        // Authentication: some tools will required, some others not,
        // we let the tool building and actions decide

        route("/sse") {
            mcp {
                val user = principalFactory.getAndSync(call)
                return@mcp mcpServerBuilder.buildMcpServer(user)
            }
        }
    }

    if (enableMcpStreamingHttp) {

        authenticate(AUTH_MEDATARUN_JWT) {
            route("/mcp") {
                install(mcpStreamableHttpBridge.createMcpSseGuard())
                post {
                    debugCall(call)
                    val principal = principalFactory.getAndSync(call)
                    mcpStreamableHttpBridge.handleStreamablePost(call) {
                        logger.debug(call.request.headers.entries().map { it.key to it.value }.toMap().toString())
                        mcpServerBuilder.buildMcpServer(principal)
                    }
                }
                delete {
                    debugCall(call)
                    mcpStreamableHttpBridge.handleStreamableDelete(call)
                }
                sse {
                    debugCall(call)
                    mcpStreamableHttpBridge.handleStreamableSse(this)
                }
            }
        }
    }

}
