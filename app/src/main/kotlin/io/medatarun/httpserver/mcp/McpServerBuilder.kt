package io.medatarun.app.io.medatarun.httpserver.mcp

import io.medatarun.app.io.medatarun.httpserver.commons.HttpAdapters
import io.medatarun.resources.ResourceInvocationException
import io.medatarun.resources.ResourceInvocationRequest
import io.medatarun.resources.ResourceRepository
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.RegisteredTool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.KType

class McpServerBuilder(private val resourceRepository: ResourceRepository) {

    private val serverInfo = Implementation(
        name = "medatarun-mcp",
        version = "1.0.0"
    )

    private val serverOptions = ServerOptions(
        capabilities = ServerCapabilities(
            tools = ServerCapabilities.Tools(listChanged = false)
        )
    )

    fun buildMcpServer(): Server {
        val server = Server(
            serverInfo = serverInfo,
            options = serverOptions
        )

        server.addTools(buildRegisteredTools())
        return server
    }


    private fun buildRegisteredTools(): List<RegisteredTool> {
        return resourceRepository.findAllDescriptors().flatMap { descriptor ->
            descriptor.commands.map { command ->
                val toolName = buildToolName(descriptor.name, command.name)
                val toolTitle = command.title ?: command.name
                val toolDescription = command.description ?: "Invoke ${descriptor.name}.${command.name}"
                val tool = Tool(
                    name = toolName,
                    title = toolTitle,
                    description = toolDescription,
                    inputSchema = buildToolInput(command),
                    outputSchema = null,
                    annotations = null
                )

                RegisteredTool(tool) { request ->
                    handleToolInvocation(descriptor.name, command.name, request)
                }
            }
        }
    }

    private suspend fun handleToolInvocation(
        resourceName: String,
        functionName: String,
        request: CallToolRequest
    ): CallToolResult {
        val rawParameters = HttpAdapters.jsonObjectToStringMap(request.arguments)
        val invocationRequest = ResourceInvocationRequest(
            resourceName = resourceName,
            functionName = functionName,
            rawParameters = rawParameters
        )

        return try {
            val result = resourceRepository.handleInvocation(invocationRequest)
            CallToolResult(
                content = listOf(TextContent(formatInvocationResult(result)))
            )
        } catch (exception: ResourceInvocationException) {
            CallToolResult(
                content = listOf(TextContent(buildMcpErrorMessage(exception))),
                isError = true
            )
        } catch (throwable: Throwable) {
            logger.error("Unhandled error while invoking $resourceName.$functionName", throwable)
            CallToolResult(
                content = listOf(
                    TextContent("Invocation failed: ${throwable.message ?: throwable::class.simpleName}")
                ),
                isError = true
            )
        }
    }

    private fun buildToolName(resourceName: String, commandName: String): String {
        val combined = "${resourceName}_${commandName}"
        val builder = StringBuilder(combined.length)
        for (char in combined) {
            builder.append(
                when (char) {
                    in 'a'..'z', in 'A'..'Z', in '0'..'9', '-', '_' -> char
                    else -> '_'
                }
            )
        }
        return builder.toString()
    }


    private fun formatInvocationResult(result: Any?): String = when (result) {
        null, Unit -> "ok"
        is String -> result
        else -> result.toString()
    }

    private fun buildMcpErrorMessage(exception: ResourceInvocationException): String {
        val parts = mutableListOf(exception.message ?: "Invocation error")
        exception.payload.forEach { (key, value) ->
            parts += "$key: $value"
        }
        return parts.joinToString(separator = "\n")
    }

    /**
     * Builds the description of the tool as the MCPInspector see it or the MCP client
     * will handle it.
     */
    private fun buildToolInput(command: ResourceRepository.ResourceCommand): Tool.Input {
        val properties = buildJsonObject {
            command.parameters.forEach { param ->
                put(param.name, buildJsonObject {
                    put("type", mapParameterType(param.type))
                })
            }
        }
        val required = command.parameters
            .filterNot { it.optional }
            .map { it.name }

        return Tool.Input(
            properties = properties,
            required = required.takeIf { it.isNotEmpty() }
        )
    }

    /**
     * Map Kotlin parameter types to the JSON Schema primitive
     * types supported by MCP tools.
     *
     * MCP supports string, number, integer, boolean, array, object, et null
     *
     * We don't support array, object (and null, because it has no meaning here).
     *
     **/
    private fun mapParameterType(parameterType: KType): String {
        val classifier = parameterType.classifier as? KClass<*> ?: return "string"
        return when (classifier) {
            Boolean::class -> "boolean"
            Int::class, Long::class, Short::class, Byte::class -> "integer"
            Double::class, Float::class -> "number"
            else -> "string"
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(McpServerBuilder::class.java)
    }

}
