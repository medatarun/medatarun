package io.medatarun.resources

import io.ktor.http.*
import io.medatarun.model.model.MedatarunException
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.*
import kotlin.reflect.full.*

class ResourceRepository(private val resources: AppResources) {

    private val resourceDescriptors = AppResources::class.memberProperties
        .filter { it.visibility == KVisibility.PUBLIC }
        .map { ResourceDescriptor(it.name, it, toCommands(it)) }
        .associateBy { it.name }


    private fun toCommands(property: KProperty1<AppResources, *>): List<ResourceCommand> {

        val resourceInstance: ResourceContainer<*> = (property.get(resources) ?: return emptyList()) as ResourceContainer<*>

        val functions = resourceInstance::class.functions
            .filter { it.name !in EXCLUDED_FUNCTIONS }
            .filter { it.hasAnnotation<ResourceCommandDoc>() }
            .map { function -> buildApiFunctionDescription(function) }

        val cmds = resourceInstance.findCommandClass()
            ?.sealedSubclasses
            ?.map { sealed -> buildApiCommandDescription(sealed) }
            ?: emptyList()

        return functions + cmds

    }

    /**
     * Builds a [ResourceCommand] based on a ModelCmd.
     *
     * At invocation time, commands are launched via the dispatch() method
     */
    private fun buildApiCommandDescription(sealed: KClass<out Any>): ResourceCommand {
        val doc = sealed.findAnnotation<ResourceCommandDoc>()
        return ResourceCommand(
            accessType = ResourceAccessType.DISPATCH,
            name = sealed.simpleName ?: "",
            title = doc?.title,
            description = doc?.description,
            resultType = typeOf<Unit>(),
            parameters = sealed.memberProperties.map {
                ResourceCommandParam(
                    name = it.name,
                    type = it.returnType,
                    optional = it.returnType.isMarkedNullable,
                )
            }

        )
    }

    private fun buildApiFunctionDescription(function: KFunction<*>): ResourceCommand {
        val metadata = function.findAnnotation<ResourceCommandDoc>()
        val parameters = function.parameters
            .filter { it.kind == KParameter.Kind.VALUE }
            .map { param ->
                ResourceCommandParam(
                    name = param.name ?: "unknown",
                    type = param.type,
                    optional = (param.isOptional || param.type.isMarkedNullable),
                )
            }
        val resultType = function.returnType
        return ResourceCommand(
            accessType = ResourceAccessType.FUNCTION,
            name = function.name,
            title = metadata?.title?.takeIf { it.isNotBlank() },
            description = metadata?.description?.takeIf { it.isNotBlank() },
            resultType = resultType,
            parameters = parameters
        )
    }

    fun findAllDescriptors(): Collection<ResourceDescriptor> {
        return resourceDescriptors.values
    }

    fun findDescriptorByIdOptional(resourceName: String): ResourceDescriptor? {
        return resourceDescriptors[resourceName]
    }


    fun handleInvocation(invocation: ResourceInvocationRequest): Any? {
        val resourceName = invocation.resourceName
        val functionName = invocation.functionName
        val rawParams = invocation.rawParameters


        val descriptor = findDescriptorByIdOptional(resourceName)
            ?: throw ResourceInvocationException(
                HttpStatusCode.NotFound,
                "Unknown resource '$resourceName'"
            )

        val resourceInstance = descriptor.property.get(resources) as ResourceContainer<Any>?
            ?: throw ResourceInvocationException(
                HttpStatusCode.InternalServerError,
                "Resource '$resourceName' unavailable"
            )


        val commands = descriptor.commands.find { it.name == functionName }
            ?: throw ResourceInvocationException(
                HttpStatusCode.NotFound,
                "Unknown function '$functionName' on '$resourceName'"
            )

        val invoker: Invoker = when (commands.accessType) {
            ResourceAccessType.FUNCTION -> createInvokerFunction(
                resourceName,
                resourceInstance,
                functionName,
                rawParams
            )

            ResourceAccessType.DISPATCH -> createInvokerDispatch(
                resourceName, resourceInstance, functionName, rawParams
            )
        }

        return try {
            invoker.invoke()
        } catch (e: InvocationTargetException) {
            val cause = e.cause
            if (cause != null) {
                logger.error("Invocation failed", e)
                throw ResourceInvocationException(
                    HttpStatusCode.InternalServerError,
                    cause::class.simpleName ?: "Invocation failed",
                    mapOf(
                        "details" to (e.cause?.message ?: e::class.simpleName ?: e).toString()
                    )
                )
            } else {
                logger.error("Invocation failed", e)
                throw ResourceInvocationException(
                    HttpStatusCode.InternalServerError,
                    "Invocation failed",
                    mapOf(
                        "details" to (e.message ?: e::class.simpleName).toString()
                    )
                )
            }
        } catch (throwable: Throwable) {
            logger.error("Invocation failed", throwable)
            throw ResourceInvocationException(
                HttpStatusCode.InternalServerError,
                "Invocation failed",
                mapOf(
                    "details" to (throwable.message ?: throwable::class.simpleName).toString()
                )
            )
        }
    }

    interface Invoker {
        fun invoke(): Any?
    }

    private fun createInvokerDispatch(
        resourceName: String,
        resourceInstance: ResourceContainer<Any>,
        functionName: String,
        rawParams: JsonObject
    ): Invoker {

        val cls = resourceInstance.findCommandClass()?.sealedSubclasses?.firstOrNull { it.simpleName == functionName }
            ?: throw ResourceInvocationException(
                HttpStatusCode.NotFound,
                "Command $functionName not found"
            )

        val function = cls.primaryConstructor ?: throw ResourceInvocationException(
            HttpStatusCode.InternalServerError,
            "Command $functionName has no primary constructor"
        )

        val callArgs = createCallArgs(resourceName, resourceInstance, function, rawParams)
        logger.debug("call args: {}", callArgs)

        return object : Invoker {
            override fun invoke(): Any? {
                val cmd = function.callBy(callArgs)
                return resourceInstance.dispatch(cmd)
            }
        }
    }

    private fun createInvokerFunction(
        resourceName: String,
        resourceInstance: ResourceContainer<Any>,
        functionName: String,
        rawParams: JsonObject
    ): Invoker {

        val function = resourceInstance::class.functions.find { it.name == functionName }
            ?: throw ResourceInvocationException(
                HttpStatusCode.NotFound,
                "Unknown function '$functionName' on '$resourceName'"
            )


        val callArgs = createCallArgs(
            resourceName, resourceInstance, function, rawParams
        )

        return object : Invoker {
            override fun invoke(): Any? {
                val result = function.callBy(callArgs)
                return result
            }


        }

    }

    fun createCallArgs(
        resourceName: String,
        resourceInstance: ResourceContainer<Any>,
        function: KFunction<*>,
        rawParams: JsonObject
    ): MutableMap<KParameter, Any?> {
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
        return callArgs
    }

    private fun convert(raw: JsonElement, classifier: Any?): ConversionResult = when (classifier) {
        Int::class -> runCatching { raw.jsonPrimitive.int }
            .fold(
                onSuccess = { ConversionResult.Value(it) },
                onFailure = { ConversionResult.Error("Parameter expecting Int cannot parse value '$raw'") }
            )

        Boolean::class -> ConversionResult.Value(raw.jsonPrimitive.boolean)
        String::class -> ConversionResult.Value(raw.jsonPrimitive.content)
        is KClass<*> -> {
            if (classifier.isValue) {
                val ctor = classifier.primaryConstructor
                    ?: return ConversionResult.Error("No constructor for value class ${classifier.simpleName}")

                val innerParam = ctor.parameters.single()
                val inner = convert(raw, innerParam.type.classifier)
                when (inner) {
                    is ConversionResult.Value ->
                        ConversionResult.Value(ctor.call(inner.value))

                    is ConversionResult.Error ->
                        inner
                }
            } else if (classifier.isData) {
                val ctor = classifier.primaryConstructor
                if (ctor == null) {
                    ConversionResult.Error("No primary constructor for data class ${classifier.simpleName}")
                } else {
                    val obj = raw.jsonObject
                    val args = mutableMapOf<KParameter, Any?>()
                    var error: ConversionResult.Error? = null

                    for (param in ctor.parameters) {
                        val field = obj[param.name]
                        if (field == null) {
                            error = ConversionResult.Error("Missing field '${param.name}' for ${classifier.simpleName}")
                            break
                        }

                        val converted = convert(field, param.type.classifier)
                        if (converted is ConversionResult.Value)
                            args[param] = converted.value
                        else if (converted is ConversionResult.Error) {
                            error = converted
                            break
                        }
                    }

                    if (error != null) error else ConversionResult.Value(ctor.callBy(args))
                }
            } else {
                ConversionResult.Error("Unsupported parameter type: ${classifier.simpleName}")
            }
        }

        else -> ConversionResult.Value(raw)
    }

    private fun buildUsageHint(resourceName: String, function: KFunction<*>): String =
        listOf(resourceName, function.name).joinToString(separator = " ") + " " + function.parameters
            .filter { it.kind == KParameter.Kind.VALUE }
            .joinToString(" ") { param -> "--${param.name}=<${param.type}>" }

    private sealed interface ConversionResult {
        data class Value(val value: Any?) : ConversionResult
        data class Error(val message: String) : ConversionResult
    }

    data class ResourceDescriptor(
        val name: String,
        val property: KProperty1<AppResources, *>,
        val commands: List<ResourceCommand>,
    )


    data class ResourceCommand(
        val name: String,
        val title: String?,
        val description: String?,
        val resultType: KType,
        val parameters: List<ResourceCommandParam>,
        val accessType: ResourceAccessType
    )


    enum class ResourceAccessType {
        /** Access is reflective, using functions */
        FUNCTION,

        /** Create an event and send it using the dispatch method */
        DISPATCH
    }

    data class ResourceCommandParam(
        val name: String,
        val type: KType,
        val optional: Boolean
    )

    companion object {
        private val EXCLUDED_FUNCTIONS = setOf("equals", "hashCode", "toString")
        private val logger = LoggerFactory.getLogger(ResourceRepository::class.java)
    }
}

data class ResourceInvocationRequest(
    val resourceName: String,
    val functionName: String,
    val rawParameters: JsonObject
)

class ResourceInvocationException(
    val status: HttpStatusCode,
    message: String,
    val payload: Map<String, String> = emptyMap()
) : MedatarunException(message)

class ResourceNotFoundException(resourceName: String) : MedatarunException("Unknown resource $resourceName")
