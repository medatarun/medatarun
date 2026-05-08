package io.medatarun.httpserver.mcp

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.exceptions.MedatarunUserException
import io.medatarun.lang.http.StatusCode

class McpMissingSessionHeaderException : MedatarunUserException("Missing MCP session header", StatusCode.BAD_REQUEST)
class McpUnknownSessionException : MedatarunUserException("Unknown MCP session", StatusCode.GONE)