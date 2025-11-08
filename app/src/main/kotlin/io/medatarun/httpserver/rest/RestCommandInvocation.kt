package io.medatarun.httpserver.rest

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.medatarun.httpserver.commons.HttpAdapters
import io.medatarun.resources.ResourceInvocationException
import io.medatarun.resources.ResourceInvocationRequest
import io.medatarun.resources.ResourceRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import org.slf4j.LoggerFactory

class RestCommandInvocation(private val resourceRepository: ResourceRepository) {

    suspend fun processInvocation(call: ApplicationCall) {
        val resourcePathValue = call.parameters["resource"]
        val functionPathValue = call.parameters["function"]
        try {
            val resourceName = resourcePathValue ?: throw ResourceInvocationException(
                HttpStatusCode.BadRequest,
                message = "Missing resource name",

                )
            val functionName = functionPathValue ?: throw ResourceInvocationException(
                HttpStatusCode.BadRequest,
                "Missing function name",

                )

            val body = call.receiveText()
            val json = if (body.isNullOrBlank()) buildJsonObject { } else Json.parseToJsonElement(body).jsonObject

            val request = ResourceInvocationRequest(
                resourceName = resourceName,
                functionName = functionName,
                rawParameters = json
            )

            val result = resourceRepository.handleInvocation(request)
            call.respond(HttpStatusCode.OK, buildResponsePayload(result))
        } catch (exception: ResourceInvocationException) {
            val resourceForLog = resourcePathValue ?: "unknown"
            val functionForLog = functionPathValue ?: "unknown"
            if (exception.status.value >= 500) {
                logger.error(
                    "Invocation failed for $resourceForLog.$functionForLog",
                    exception
                )
            } else {
                logger.warn(
                    "Invocation error for $resourceForLog.$functionForLog: ${exception.message}"
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
        else -> mapOf("status" to "ok", "result" to result.toString())
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RestCommandInvocation::class.java)
    }
}