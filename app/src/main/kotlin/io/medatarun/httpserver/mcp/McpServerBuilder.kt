package io.medatarun.httpserver.mcp


import io.medatarun.actions.ports.needs.ActionRequest
import io.medatarun.actions.runtime.*
import io.medatarun.security.AppPrincipal
import io.metadatarun.ext.config.actions.ConfigAgentInstructions
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.RegisteredTool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType

// Json serialization for tool responses
private val json = Json { prettyPrint = false }

class McpServerBuilder(
    private val actionRegistry: ActionRegistry,
    private val actionInvoker: ActionInvoker,
    private val configAgentInstructions: ConfigAgentInstructions,
    private val actionCtxFactory: ActionCtxFactory,
) {

    private val serverInfo = Implementation(
        name = "medatarun",
        version = "1.0.0",
    )

    private val serverOptions = ServerOptions(
        capabilities = ServerCapabilities(
            tools = ServerCapabilities.Tools(listChanged = false),
        )
    )

    fun buildMcpServer(user: AppPrincipal?): Server {
        val server = Server(
            serverInfo = serverInfo,
            options = serverOptions,
            ) {
            configAgentInstructions.process()
        }

        val toolsToAdd = buildRegisteredTools(user)
        server.addTools(toolsToAdd)
        return server
    }


    private fun buildRegisteredTools(user: AppPrincipal?): List<RegisteredTool> {
        return actionRegistry.findAllGroupDescriptors().flatMap { group ->
            group.actions.map { action ->
                val toolName = buildToolName(group.key, action.key)
                val toolTitle = action.title ?: action.key
                val toolDescription = action.description ?: "Invoke ${group.key}.${action.key}"
                val tool = Tool(
                    name = toolName,
                    title = toolTitle,
                    description = toolDescription,
                    inputSchema = buildToolInput(action),
                    outputSchema = null,
                    annotations = null
                )

                RegisteredTool(tool) { request ->
                    handleToolInvocation(group.key, action.key, request, user)
                }
            }
        }
    }

    private suspend fun handleToolInvocation(
        actionGroupKey: String,
        actionKey: String,
        request: CallToolRequest,
        principal: AppPrincipal?
    ): CallToolResult {

        val invocationRequest = ActionRequest(
            actionGroupKey = actionGroupKey,
            actionKey = actionKey,
            payload = request.arguments
        )

        return try {
            val result = actionInvoker.handleInvocation(invocationRequest, actionCtxFactory.create(principal))
            toCallToolResult(result)
        } catch (exception: ActionInvocationException) {
            CallToolResult(
                content = listOf(TextContent(buildMcpErrorMessage(exception))),
                isError = true
            )
        } catch (throwable: Throwable) {
            logger.error("Unhandled error while invoking $actionGroupKey.$actionKey", throwable)
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

    private fun toCallToolResult(result: Any?): CallToolResult {
        return when(result) {
            // When no result is provided, returns "ok" to the agent as a string
            // the protocol doesn't specify what to do when the tool only acts and doesn't have content to return
            null, Unit -> CallToolResult(content = listOf(TextContent("ok")))
            // If the action response is a plain String then we consider this is text only
            is String -> CallToolResult(content=listOf(TextContent(result)))
            // If the action response is a structured object, we return it as a JSON object
            // it should be in "content" as plain text and also in "structuredContent" as JSON object
            // as specified in protocol
            is JsonObject -> CallToolResult(
                content=listOf(TextContent(result.toString())),
                structuredContent = result
            )
            // If the action response is an array, the protocol doesn't accept arrays as is
            // so we wrap it in a JSON object with "items" key and return it as "structuredContent"
            // As specified in the protocol, we should also return it as plain text in "content"
            is JsonArray -> {
                val json = buildJsonObject { put("items", result) }
                CallToolResult(
                    content=listOf(TextContent(json.toString())),
                    structuredContent = json
                )
            }
            is Iterable<*> -> {
                val items = buildJsonArray {
                    for (item in result) {
                        if (item == null) {
                            add(JsonNull)
                        } else {
                            add(encodeAnyToJsonElement(item))
                        }
                    }
                }
                val json = buildJsonObject { put("items", items) }
                CallToolResult(
                    content=listOf(TextContent(json.toString())),
                    structuredContent = json
                )
            }
            // Else we apply Json serialization to the result and return it as plain text
            // and consider it is Json (typical of serializable classes returned by actions)
            else -> {
                val json = encodeAnyToJsonElement(result)
                val jsonObject = when(json) {
                    is JsonObject -> json
                    else -> JsonObject(mapOf("result" to json))
                }
                CallToolResult(
                    content=listOf(TextContent(jsonObject.toString())),
                    structuredContent = jsonObject
                )
            }
        }
    }

    private fun buildMcpErrorMessage(exception: ActionInvocationException): String {
        val parts = mutableListOf(exception.message ?: "Invocation error")
        exception.payload.forEach { (key, value) ->
            parts += "$key: $value"
        }
        return parts.joinToString(separator = "\n")
    }

    private fun encodeAnyToJsonElement(value: Any): JsonElement {
        // Use the runtime type to avoid trying to serialize as Any? (which has no serializer).
        val serializer = json.serializersModule.serializer(value::class.createType()) as KSerializer<Any>
        return json.encodeToJsonElement(serializer, value)
    }

    /**
     * Builds the description of the tool as the MCPInspector see it or the MCP client
     * will handle it.
     */
    private fun buildToolInput(actionDescriptor: ActionCmdDescriptor): Tool.Input {
        val properties = buildJsonObject {
            actionDescriptor.parameters.forEach { param ->
                put(param.name, buildJsonObject {
                    put("type", mapParameterType(param.type))
                })
            }
        }
        val required = actionDescriptor.parameters
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
