package io.medatarun.httpserver.rest

import io.medatarun.actions.domain.ActionRegistry
import kotlinx.serialization.json.*
import java.net.URI

class RestApiDoc(
    private val actionRegistry: ActionRegistry,
    private val publicBaseUrl: URI
) {

    /**
     * Builds a minimal OpenAPI document compatible with generic clients.
     *
     * Every action is exposed as:
     * POST /api/{actionGroupKey}/{actionKey}
     */
    fun buildApiDescription(): JsonObject {
        val actions = actionRegistry.findAllActions()
        return buildJsonObject {
            put("openapi", "3.1.0")
            putJsonObject("info") {
                put("title", "Medatarun Actions API")
                put("version", "1.0.0")
                put(
                    "description",
                    "This OpenAPI description is intentionally minimal and focused on technical compatibility. " +
                        "For complete functional documentation of actions and parameters (intent, constraints, examples, context), " +
                        "use the Medatarun UI Action runner or the CLI with `medatarun help`. " +
                        "The full application documentation is available at https://docs.medatarun.com/."
                )
            }
            putJsonArray("servers") {
                add(
                    buildJsonObject {
                        put("url", publicBaseUrl.toString()+"api/")
                    }
                )
            }
            putJsonObject("paths") {
                actions.forEach { action ->
                    val descriptor = action.descriptor
                    val path = "/${descriptor.group}/${descriptor.key}"
                    putJsonObject(path) {
                        putJsonObject("post") {
                            descriptor.title?.let { put("summary", it) }
                            descriptor.description?.let { put("description", it) }
                            put("operationId", "${descriptor.group}_${descriptor.key}")
                            putJsonArray("tags") {
                                add(JsonPrimitive(descriptor.group))
                            }
                            putJsonObject("requestBody") {
                                put("required", true)
                                putJsonObject("content") {
                                    putJsonObject("application/json") {
                                        putJsonObject("schema") {
                                            put("type", "object")
                                            putJsonObject("properties") {
                                                descriptor.parameters
                                                    .sortedBy { it.order }
                                                    .forEach { parameter ->
                                                        putJsonObject(parameter.key) {
                                                            put("type", parameter.jsonType.code)
                                                            parameter.description?.let { put("description", it) }
                                                        }
                                                    }
                                            }
                                            val requiredParams = descriptor.parameters
                                                .sortedBy { it.order }
                                                .filter { !it.optional }
                                                .map { it.key }
                                            if (requiredParams.isNotEmpty()) {
                                                putJsonArray("required") {
                                                    requiredParams.forEach { add(JsonPrimitive(it)) }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            putJsonObject("responses") {
                                putJsonObject("200") {
                                    put("description", "Successful response")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
