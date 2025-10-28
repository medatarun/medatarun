package io.medatarun.resources

import io.ktor.http.*
import io.medatarun.model.model.MedatarunException
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.*
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties

class ResourceRepository(private val resources: AppResources) {

    private val resourceDescriptors = AppResources::class.memberProperties
        .filter { it.visibility == KVisibility.PUBLIC }
        .map { ResourceDescriptor(it.name, it, toCommands(it)) }
        .associateBy { it.name }


    private fun toCommands(property: KProperty1<AppResources, *>): List<ResourceCommand> {

        val resourceInstance: ResourceContainer = (property.get(resources) ?: return emptyList()) as ResourceContainer

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
    private fun buildApiCommandDescription(sealed: KClass<out Any>): ResourceCommand = ResourceCommand(
        accessType = ResourceAccessType.DISPATCH,
        name = sealed.simpleName ?: "",
        description = null,
        title = null,
        resultType = typeOf<Unit>(),
        parameters = sealed.memberProperties.map {
            ResourceCommandParam(
                name = it.name,
                type = it.returnType,
                optional = it.returnType.isMarkedNullable,
            )
        }

    )

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

        val resourceInstance = descriptor.property.get(resources)
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

            ResourceAccessType.DISPATCH -> createInvokerDispatch()
        }

        return try {
            invoker.invoke()
        } catch (e: InvocationTargetException) {
            val cause = e.cause
            if (cause != null) {
                throw ResourceInvocationException(
                    HttpStatusCode.InternalServerError,
                    cause::class.simpleName ?: "Invocation failed",
                    mapOf(
                        "details" to (e.cause?.message ?: e::class.simpleName ?: e).toString()
                    )
                )
            } else {
                throw ResourceInvocationException(
                    HttpStatusCode.InternalServerError,
                    "Invocation failed",
                    mapOf(
                        "details" to (e.message ?: e::class.simpleName).toString()
                    )
                )
            }
        } catch (throwable: Throwable) {
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

    private fun createInvokerDispatch(): Invoker {
        return object : Invoker {
            override fun invoke() {
                TODO("Not yet implemented")
            }
        }
    }

    private fun createInvokerFunction(
        resourceName: String,
        resourceInstance: Any,
        functionName: String,
        rawParams: Map<String, String>
    ): Invoker {

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

        return object : Invoker {
            override fun invoke(): Any? {
                val result = function.callBy(callArgs)
                return result
            }


        }

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
    }
}

data class ResourceInvocationRequest(
    val resourceName: String,
    val functionName: String,
    val rawParameters: Map<String, String>
)

class ResourceInvocationException(
    val status: HttpStatusCode,
    message: String,
    val payload: Map<String, String> = emptyMap()
) : MedatarunException(message)

class ResourceNotFoundException(resourceName: String) : MedatarunException("Unknown resource $resourceName")
