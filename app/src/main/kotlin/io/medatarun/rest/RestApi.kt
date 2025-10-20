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
import io.medatarun.model.model.MedatarunException
import io.medatarun.runtime.AppRuntime
import io.medatarun.runtime.getLogger
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.functions

/**
 * REST API server that mirrors the CLI reflection behaviour on top of Ktor.
 */
class RestApi(
    private val runtime: AppRuntime,
) {
    private val logger = getLogger(RestApi::class)
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
        try {
            val invocation = call.toInvocationRequest()
            val payload = handleInvocation(invocation)
            call.respond(HttpStatusCode.OK, payload)
        } catch (exception: ResourceInvocationException) {
            call.respond(exception.status, exception.payload)
        }
    }


    private suspend fun ApplicationCall.toInvocationRequest(): ResourceInvocationRequest {
        val resourceName = parameters["resource"] ?: throw ResourceInvocationException(
            HttpStatusCode.BadRequest,
            message = "Missing resource name",

            )
        val functionName = parameters["function"] ?: throw ResourceInvocationException(
            HttpStatusCode.BadRequest,
            "Missing function name",

            )

        val rawParams = mutableMapOf<String, String>()
        rawParams.putAll(request.queryParameters.toSingleValueMap())
        rawParams.putAll(readBodyParameters())

        return ResourceInvocationRequest(
            resource = resourceName,
            function = functionName,
            parameters = rawParams.toMap()
        )
    }

    fun handleInvocation(invocation: ResourceInvocationRequest): Any {
        val (resourceName, functionName, rawParams) = invocation

        resourceRepository.findDescriptorByIdOptional(resourceName)
            ?: throw ResourceInvocationException(
                HttpStatusCode.NotFound,
                "Unknown resource '$resourceName'"

            )

        val resourceInstance = resourceRepository.findResourceInstanceById(resourceName)
            ?: throw ResourceInvocationException(
                HttpStatusCode.InternalServerError,
                "Resource '$resourceName' unavailable"
            )

        val function = resourceInstance::class.functions.find { it.name == functionName }
            ?: throw ResourceInvocationException(
                HttpStatusCode.NotFound,
                "Unknown function '$functionName' on '$resourceName'"
            )

        val missing = function.parameters
            .filter { it.kind == KParameter.Kind.VALUE && !it.isOptional && it.name !in rawParams.keys }
            .mapNotNull { it.name }

        if (missing.isNotEmpty()) {
            throw ResourceInvocationException(
                HttpStatusCode.BadRequest,
                "Missing parameter(s): ${missing.joinToString(", ")}",
                mapOf(
                    "usage" to buildUsageHint(resourceName, function)
                )
            )
        }

        val callArgs = mutableMapOf<KParameter, Any?>()
        val conversionErrors = mutableListOf<String>()

        function.parameters.forEach { parameter ->
            when (parameter.kind) {
                KParameter.Kind.INSTANCE -> callArgs[parameter] = resourceInstance
                KParameter.Kind.VALUE -> {
                    val raw = parameter.name?.let(rawParams::get)
                    if (raw != null) {
                        when (val conversion = convert(raw, parameter.type.classifier)) {
                            is ConversionResult.Error -> conversionErrors += conversion.message
                            is ConversionResult.Value -> callArgs[parameter] = conversion.value
                        }
                    }
                }

                else -> Unit
            }
        }

        if (conversionErrors.isNotEmpty()) {
            throw ResourceInvocationException(
                HttpStatusCode.BadRequest,
                "Invalid parameter values",
                mapOf(

                    "details" to conversionErrors.joinToString(", ")
                )
            )
        }

        val result = runCatching { function.callBy(callArgs) }
            .onFailure { throwable ->
                logger.error("Invocation failed for $resourceName.$functionName", throwable)
            }
            .getOrElse {
                throw ResourceInvocationException(
                    HttpStatusCode.InternalServerError,
                    "Invocation failed",
                    mapOf(
                        "details" to (it.message ?: it::class.simpleName).toString()
                    )
                )
            }

        return when (result) {
            null, Unit -> mapOf("status" to "ok")
            is String -> result.toString()
            else -> mapOf("status" to "ok", "result" to result.toString())
        }
    }

    private suspend fun ApplicationCall.readBodyParameters(): Map<String, String> {
        if (!request.httpMethod.allowsBody()) return emptyMap()
        val contentType = request.contentType()
        return when {
            contentType.match(ContentType.Application.Json) ->
                runCatching { receiveNullable<JsonObject>() }.getOrNull()?.toStringMap().orEmpty()

            contentType.match(ContentType.Application.FormUrlEncoded) ->
                runCatching { receiveParameters().toSingleValueMap() }.getOrNull().orEmpty()

            else -> emptyMap()
        }
    }

    private fun JsonObject.toStringMap(): Map<String, String> =
        entries.mapNotNull { (key, value) -> value.toPrimitiveString()?.let { key to it } }.toMap()

    private fun JsonElement.toPrimitiveString(): String? = when (this) {
        is JsonPrimitive -> this.contentOrNull ?: this.toString()
        else -> this.toString()
    }

    private fun convert(raw: String, classifier: Any?): ConversionResult = when (classifier) {
        Int::class -> runCatching { raw.toInt() }
            .fold(
                onSuccess = { ConversionResult.Value(it) },
                onFailure = { ConversionResult.Error("Parameter expecting Int cannot parse value '$raw'") }
            )

        Boolean::class -> ConversionResult.Value(raw.toBoolean())

        String::class -> ConversionResult.Value(raw)
        is KClass<*> -> ConversionResult.Value(raw)
        else -> ConversionResult.Value(raw)
    }

    private fun Parameters.toSingleValueMap(): Map<String, String> =
        entries().mapNotNull { entry ->
            entry.value.lastOrNull()?.let { entry.key to it }
        }.toMap()

    private fun HttpMethod.allowsBody(): Boolean =
        this == HttpMethod.Post || this == HttpMethod.Put || this == HttpMethod.Patch || this == HttpMethod.Delete

    private fun buildUsageHint(resourceName: String, function: KFunction<*>): String =
        listOf(resourceName, function.name).joinToString(separator = " ") + " " + function.parameters
            .filter { it.kind == KParameter.Kind.VALUE }
            .joinToString(" ") { param -> "--${param.name}=<${param.type}>" }

    private sealed interface ConversionResult {
        data class Value(val value: Any?) : ConversionResult
        data class Error(val message: String) : ConversionResult
    }

    companion object {
        private val EXCLUDED_FUNCTIONS = setOf("equals", "hashCode", "toString")
    }
}

@Serializable
data class ApiDescriptionFunction(
    val name: String,
    val parameters: List<ApiDescriptionParam>
)

@Serializable
data class ApiDescriptionParam(val name: String, val type: String, val optional: Boolean)
