package io.medatarun.httpserver.rest

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.medatarun.actions.domain.ActionInvocationException
import io.medatarun.actions.domain.ActionInvoker
import io.medatarun.actions.ports.needs.ActionPayload
import io.medatarun.actions.ports.needs.ActionRequest
import io.medatarun.actions.runtime.ActionCtxFactory
import io.medatarun.httpserver.commons.HttpAdapters
import io.medatarun.lang.http.StatusCode
import io.medatarun.security.AppPrincipal
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory

class RestCommandInvocation(
    private val actionInvoker: ActionInvoker,
    private val actionCtxFactory: ActionCtxFactory
) {

    suspend fun processInvocation(call: ApplicationCall, principal: AppPrincipal?) {
        val actionGroupKeyPathValue = call.parameters["actionGroupKey"]
        val actionKeyPathValue = call.parameters["actionKey"]
        try {
            val actionGroupKey = actionGroupKeyPathValue ?: throw ActionInvocationException(
                StatusCode.BAD_REQUEST,
                message = "Missing resource name",

                )
            val actionKey = actionKeyPathValue ?: throw ActionInvocationException(
                StatusCode.BAD_REQUEST,
                "Missing function name",

                )

            val body = call.receiveText()
            val json = if (body.isNullOrBlank()) buildJsonObject { } else Json.parseToJsonElement(body).jsonObject

            val request = ActionRequest(
                actionGroupKey = actionGroupKey,
                actionKey = actionKey,
                payload = ActionPayload.AsJson(json)
            )

            val result = actionInvoker.handleInvocation(request, actionCtxFactory.create(principal))
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
            if (exception.status.httpStatusCode >= 500) {
                logger.error(
                    "Invocation failed for $resourceForLog.$functionForLog",
                    exception
                )
            } else {
                logger.warn(
                    "Invocation error for $resourceForLog/$functionForLog: ${exception.message}"
                )
            }

            val payloadJson = buildJsonObject {
                put("type", "about:blank")
                put("title", exception.msg)
                put("status", exception.status.httpStatusCode)
                exception.payload.forEach { (k, v) ->
                    put(k,v)
                }
            }
            call.respondText(
                status = HttpStatusCode(exception.status.httpStatusCode, exception.status.message),
                text = payloadJson.toString(),
                contentType = ContentType.Application.Json
            )
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