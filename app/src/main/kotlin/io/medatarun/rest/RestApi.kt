package io.medatarun.rest

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.medatarun.app.io.medatarun.resources.ResourceInvocationException
import io.medatarun.app.io.medatarun.resources.ResourceInvocationRequest
import io.medatarun.app.io.medatarun.resources.ResourceRepository
import io.medatarun.cli.AppCLIResources
import io.medatarun.runtime.AppRuntime
import io.medatarun.runtime.getLogger
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import org.slf4j.LoggerFactory

/**
 * REST API server that mirrors the CLI reflection behaviour on top of Ktor.
 */
class RestApi(
    private val runtime: AppRuntime,
) {
    private val logger = LoggerFactory.getLogger(RestApi::class.java)
    private val resources = AppCLIResources(runtime)
    private val resourceRepository = ResourceRepository(resources)

    @Volatile
    private var engine: EmbeddedServer<*, *>? = null

    /**
     * Starts the REST API server. Subsequent calls while the server is running throw an [IllegalStateException].
     */
    fun start(
        host: String = "0.0.0.0",
        port: Int = 8080,
        wait: Boolean = false,
    ) {
        synchronized(this) {
            check(engine == null) { "RestApi server already running" }
            engine = embeddedServer(Netty, host = host, port = port, module = { configure() }).also {
                logger.info("Starting REST API on http://$host:$port")
                it.start(wait = wait)
            }
        }
    }

    /**
     * Stops the REST API server if it is running.
     */
    fun stop(gracePeriodMillis: Long = 1_000, timeoutMillis: Long = 2_000) {
        synchronized(this) {
            engine?.let {
                logger.info("Stopping REST API")
                it.stop(gracePeriodMillis, timeoutMillis)
            }
            engine = null
        }
    }

    private fun Application.configure() {
        install(ContentNegotiation) { json() }

        routing {
            get("/health") {
                call.respond(mapOf("status" to "ok"))
            }

            get("/api") {
                call.respond(buildApiDescription())
            }

            route("/api/{resource}/{function}") {
                get { processInvocation(call) }
                post { processInvocation(call) }
            }
        }
    }

    fun buildApiDescription(): Map<String, List<ApiDescriptionFunction>> {
        return resourceRepository
            .findAllDescriptors().associate { res ->
                res.name to res.commands.map { cmd ->
                    ApiDescriptionFunction(
                        cmd.name, cmd.parameters.map { p ->
                            ApiDescriptionParam(
                                name = p.name,
                                type = p.type,
                                optional = p.optional
                            )
                        }
                    )
                }
            }
    }


    private suspend fun processInvocation(call: ApplicationCall) {
        var resourceForLog = call.parameters["resource"]
        var functionForLog = call.parameters["function"]
        try {
            val resourceName = requirePathParameter(call.parameters, "resource", "Missing resource name")
            val functionName = requirePathParameter(call.parameters, "function", "Missing function name")
            resourceForLog = resourceName
            functionForLog = functionName

            val rawParameters = LinkedHashMap<String, String>()
            rawParameters.putAll(toSingleValueMap(call.request.queryParameters))
            rawParameters.putAll(readBodyParameters(call))

            val request = ResourceInvocationRequest(
                resourceName = resourceName,
                functionName = functionName,
                rawParameters = rawParameters
            )

            logger.info(request.toString())

            val result = resourceRepository.handleInvocation(request)
            call.respond(HttpStatusCode.OK, buildResponsePayload(result))
        } catch (exception: ResourceInvocationException) {
            val resource = resourceForLog ?: "unknown"
            val function = functionForLog ?: "unknown"
            if (exception.status.value >= 500) {
                logger.error(
                    "Invocation failed for $resource.$function",
                    exception
                )
            } else {
                logger.warn(
                    "Invocation error for $resource.$function: ${exception.message}"
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
                runCatching { call.receiveNullable<JsonObject>() }.getOrNull()?.let(::jsonObjectToStringMap).orEmpty()

            contentType.match(ContentType.Application.FormUrlEncoded) ->
                runCatching { call.receiveParameters() }
                    .map { parameters -> toSingleValueMap(parameters) }
                    .getOrNull()
                    .orEmpty()

            else -> emptyMap()
        }
    }

    private fun jsonObjectToStringMap(jsonObject: JsonObject): Map<String, String> =
        jsonObject.entries.mapNotNull { (key, value) -> toPrimitiveString(value)?.let { key to it } }.toMap()

    private fun toPrimitiveString(element: JsonElement): String? = when (element) {
        is JsonPrimitive -> element.contentOrNull ?: element.toString()
        else -> element.toString()
    }

    private fun toSingleValueMap(parameters: Parameters): Map<String, String> =
        parameters.entries().mapNotNull { entry ->
            entry.value.lastOrNull()?.let { value -> entry.key to value }
        }.toMap()

    private fun allowsBody(method: HttpMethod): Boolean =
        method == HttpMethod.Post || method == HttpMethod.Put || method == HttpMethod.Patch || method == HttpMethod.Delete

    private fun requirePathParameter(parameters: Parameters, name: String, missingMessage: String): String =
        parameters[name] ?: throw ResourceInvocationException(HttpStatusCode.BadRequest, missingMessage)

    private fun buildResponsePayload(result: Any?): Any = when (result) {
        null, Unit -> mapOf("status" to "ok")
        is String -> result
        else -> mapOf("status" to "ok", "result" to result.toString())
    }
}

@Serializable
data class ApiDescriptionFunction(
    val name: String,
    val parameters: List<ApiDescriptionParam>
)

@Serializable
data class ApiDescriptionParam(val name: String, val type: String, val optional: Boolean)
