package io.medatarun.httpserver.mcp

import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.medatarun.httpserver.commons.AppPrincipalFactory
import io.modelcontextprotocol.kotlin.sdk.server.mcp

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
        route("/mcp") {
            post {
                val principal = principalFactory.getAndSync(call)
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
