package io.medatarun.httpserver.mcp

import io.modelcontextprotocol.kotlin.sdk.JSONRPCMessage

internal sealed interface StreamableHttpResponse {
    data class Json(val message: JSONRPCMessage) : StreamableHttpResponse
    data object Accepted : StreamableHttpResponse
}