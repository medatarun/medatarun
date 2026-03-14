package io.medatarun.httpserver.rest

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.medatarun.actions.domain.ActionExceptionInterpreter
import io.medatarun.actions.domain.ActionInvocationException
import io.medatarun.actions.domain.ActionInvoker
import io.medatarun.actions.ports.needs.ActionPayload
import io.medatarun.actions.ports.needs.ActionRequest
import io.medatarun.actions.runtime.ActionRequestCtxFactory
import io.medatarun.lang.http.StatusCode
import io.medatarun.security.AppPrincipal
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory

class RestCommandInvocation(
    private val actionInvoker: ActionInvoker,
    private val actionRequestCtxFactory: ActionRequestCtxFactory
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
            val json = if (body.isBlank()) buildJsonObject { } else Json.parseToJsonElement(body).jsonObject

            val request = ActionRequest(
                actionGroupKey = actionGroupKey,
                actionKey = actionKey,
                payload = ActionPayload.AsJson(json)
            )

            val result = actionInvoker.handleInvocation(request, actionRequestCtxFactory.create(principal))
            val responsePayload = buildResponsePayload(result)
            when (responsePayload) {
                is String -> call.respondText(responsePayload, ContentType.Text.Plain)
                is JsonObject, is JsonArray -> call.respondText(
                    responsePayload.toString(),
                    ContentType.Application.Json
                )

                else -> call.respond(responsePayload)
            }
            call.respond(HttpStatusCode.OK, responsePayload)
        } catch (exception: Throwable) {
            val resourceForLog = actionGroupKeyPathValue ?: "unknown"
            val functionForLog = actionKeyPathValue ?: "unknown"
            val i = ActionExceptionInterpreter(exception)
            if (i.isInternal()) {
                logger.error(
                    "Invocation failed for $resourceForLog.$functionForLog",
                    exception
                )
            } else {
                logger.warn(
                    "Invocation error for $resourceForLog/$functionForLog: ${i.privateErrorMessage()}"
                )
            }

            val payloadJson = buildJsonObject {
                put("type", "about:blank")
                put("title", i.publicErrorMessage())
                put("status", i.statusCode)
                i.details().forEach { (k, v) ->
                    put(k, v)
                }
            }
            call.respondText(
                status = HttpStatusCode(i.statusCode, i.statusName),
                text = payloadJson.toString(),
                contentType = ContentType.Application.Json
            )
        }
    }

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