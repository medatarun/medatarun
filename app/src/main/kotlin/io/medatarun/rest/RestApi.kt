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
import io.medatarun.cli.AppCLIResources
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
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.functions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * REST API server that mirrors the CLI reflection behaviour on top of Ktor.
 */
class RestApi(
    private val runtime: AppRuntime,
) {
    private val logger = getLogger(RestApi::class)
    private val resources = AppCLIResources(runtime)
    private val resourceProperties: Map<String, KProperty1<AppCLIResources, *>> =
        AppCLIResources::class.memberProperties
            .filter { it.visibility == KVisibility.PUBLIC }
            .associateBy { it.name }

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
                get {
                    handleInvocation(call)
                }
                post {
                    handleInvocation(call)
                }
            }
        }
    }

    private fun buildApiDescription(): Map<String, List<ApiDescriptionFunction>> {
        logger.info("Building API description")
        val result = resourceProperties
            .mapValues { (_, property) ->
                val resourceInstance = property.get(resources) ?: return@mapValues emptyList()
                val functions = resourceInstance::class.functions
                    .filter { it.name !in EXCLUDED_FUNCTIONS }
                    .map { function -> buildApiFunctionDescription(function) }
                return@mapValues functions
            }
        logger.info("test" + result.toString())
        return result
    }

    private fun buildApiFunctionDescription(function: KFunction<*>): ApiDescriptionFunction = ApiDescriptionFunction(
        name = function.name,
        parameters = function.parameters
            .filter { it.kind == KParameter.Kind.VALUE }
            .map { param ->
                ApiDescriptionParam(
                    name = param.name ?: "unknown",
                    type = param.type.toString(),
                    optional = (param.isOptional || param.type.isMarkedNullable),
                )
            }
    )


    private suspend fun handleInvocation(call: ApplicationCall) {
        val resourceName = call.parameters["resource"] ?: run {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing resource name"))
            return
        }
        val functionName = call.parameters["function"] ?: run {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing function name"))
            return
        }

        val property = resourceProperties[resourceName] ?: run {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Unknown resource '$resourceName'"))
            return
        }

        val resourceInstance = property.get(resources) ?: run {
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Resource '$resourceName' unavailable"))
            return
        }

        val function = resourceInstance::class.functions.find { it.name == functionName } ?: run {
            call.respond(
                HttpStatusCode.NotFound,
                mapOf("error" to "Unknown function '$functionName' on '$resourceName'")
            )
            return
        }

        val rawParams = mutableMapOf<String, String>()
        rawParams.putAll(call.request.queryParameters.toSingleValueMap())
        rawParams.putAll(call.readBodyParameters())

        val missing = function.parameters
            .filter { it.kind == KParameter.Kind.VALUE && !it.isOptional && it.name !in rawParams.keys }
            .mapNotNull { it.name }
        if (missing.isNotEmpty()) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf(
                    "error" to "Missing parameter(s): ${missing.joinToString(", ")}",
                    "usage" to buildUsageHint(resourceName, function)
                )
            )
            return
        }

        val callArgs = mutableMapOf<KParameter, Any?>()
        val conversionErrors = mutableListOf<String>()

        function.parameters.forEach { parameter ->
            when (parameter.kind) {
                KParameter.Kind.INSTANCE -> callArgs[parameter] = resourceInstance
                KParameter.Kind.VALUE -> {
                    val raw = parameter.name?.let(rawParams::get)
                    if (raw != null) {
                        val conversion = convert(raw, parameter.type.classifier)
                        if (conversion is ConversionResult.Error) {
                            conversionErrors += conversion.message
                        } else if (conversion is ConversionResult.Value) {
                            callArgs[parameter] = conversion.value
                        } else {
                            callArgs[parameter] = null
                        }
                    }
                }

                else -> Unit
            }
        }

        if (conversionErrors.isNotEmpty()) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf(
                    "error" to "Invalid parameter values",
                    "details" to conversionErrors
                )
            )
            return
        }

        val result = runCatching { function.callBy(callArgs) }.onFailure { throwable ->
            logger.error("Invocation failed for $resourceName.$functionName", throwable)
        }.getOrElse {
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf(
                    "error" to "Invocation failed",
                    "message" to (it.message ?: it::class.simpleName)
                )
            )
            return
        }

        val payload = when (result) {
            null, Unit -> mapOf("status" to "ok")
            is String -> result.toString()
            else -> mapOf("status" to "ok", "result" to result.toString())
        }

        call.respond(HttpStatusCode.OK, payload)
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