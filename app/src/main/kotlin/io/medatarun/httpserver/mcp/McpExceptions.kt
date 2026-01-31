package io.medatarun.httpserver.mcp

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.http.StatusCode

class McpMissingSessionHeaderException : MedatarunException("Missing MCP session header", StatusCode.BAD_REQUEST)
class McpUnknownSessionException : MedatarunException("Unknown MCP session", StatusCode.GONE)