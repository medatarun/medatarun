package io.medatarun.httpserver.rest

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.medatarun.actions.ports.needs.ActionRequest
import io.medatarun.actions.runtime.ActionCtxFactory
import io.medatarun.actions.runtime.ActionInvocationException
import io.medatarun.actions.runtime.ActionRegistry
import io.medatarun.httpserver.commons.HttpAdapters
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory

class RestCommandInvocation(
    private val actionRegistry: ActionRegistry,
    private val actionCtxFactory: ActionCtxFactory
) {

    suspend fun processInvocation(call: ApplicationCall) {
        val actionGroupKeyPathValue = call.parameters["actionGroupKey"]
        val actionKeyPathValue = call.parameters["actionKey"]
        try {
            val actionGroupKey = actionGroupKeyPathValue ?: throw ActionInvocationException(
                HttpStatusCode.BadRequest,
                message = "Missing resource name",

                )
            val actionKey = actionKeyPathValue ?: throw ActionInvocationException(
                HttpStatusCode.BadRequest,
                "Missing function name",

                )

            val body = call.receiveText()
            val json = if (body.isNullOrBlank()) buildJsonObject { } else Json.parseToJsonElement(body).jsonObject

            val request = ActionRequest(
                actionGroupKey = actionGroupKey,
                actionKey = actionKey,
                payload = json
            )

            val result = actionRegistry.handleInvocation(request, actionCtxFactory.create())
            val responsePayload = buildResponsePayload(result)
            when (responsePayload) {
                is String -> call.respondText(responsePayload, ContentType.Text.Plain)
                is JsonObject, is JsonArray -> call.respondText(responsePayload.toString(), ContentType.Application.Json)
                else -> call.respond(responsePayload)
            }
            call.respond(HttpStatusCode.OK, responsePayload)
        } catch (exception: ActionInvocationException) {
            val resourceForLog = actionGroupKeyPathValue ?: "unknown"
            val functionForLog = actionKeyPathValue ?: "unknown"
            if (exception.status.value >= 500) {
                logger.error(
                    "Invocation failed for $resourceForLog.$functionForLog",
                    exception
                )
            } else {
                logger.warn(
                    "Invocation error for $resourceForLog/$functionForLog: ${exception.message}"
                )
            }
            call.respond(exception.status, exception.payload)
        }
    }

    private suspend fun readBodyParameters(call: ApplicationCall): Map<String, String> {
        if (!allowsBody(call.request.httpMethod)) return emptyMap()
        val contentType = call.request.contentType()
        return when {
            contentType.match(ContentType.Application.Json) ->
                runCatching { call.receiveNullable<JsonObject>() }.getOrNull()?.let(HttpAdapters::jsonObjectToStringMap)
                    .orEmpty()

            contentType.match(ContentType.Application.FormUrlEncoded) ->
                runCatching { call.receiveParameters() }
                    .map { parameters -> toSingleValueMap(parameters) }
                    .getOrNull()
                    .orEmpty()

            else -> emptyMap()
        }
    }


    private fun toSingleValueMap(parameters: Parameters): Map<String, String> =
        parameters.entries().mapNotNull { entry ->
            entry.value.lastOrNull()?.let { value -> entry.key to value }
        }.toMap()

    private fun allowsBody(method: HttpMethod): Boolean =
        method == HttpMethod.Post || method == HttpMethod.Put || method == HttpMethod.Patch || method == HttpMethod.Delete

    private fun buildResponsePayload(result: Any?): Any = when (result) {
        null, Unit -> mapOf("status" to "ok")
        is String -> result
        is JsonObject -> result
        is JsonArray -> result
        else -> result
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RestCommandInvocation::class.java)
    }
}