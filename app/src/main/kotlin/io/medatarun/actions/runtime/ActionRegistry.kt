package io.medatarun.actions.runtime

import io.ktor.http.*
import io.medatarun.actions.ports.needs.*
import io.medatarun.actions.providers.ActionProviders
import io.medatarun.kernel.ExtensionRegistry
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.*
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

class ActionRegistry(private val actionProviders: ActionProviders, private val extensionRegistry: ExtensionRegistry) {

    private val actionProviderContributions = extensionRegistry.findContributionsFlat(ActionProvider::class)

    private val actionGroupDescriptors: List<ActionGroupDescriptor> =
        ActionProviders::class.memberProperties
            .filter { it.visibility == KVisibility.PUBLIC }
            .map { toActionProviderInstance(it) }
            .map {
                ActionGroupDescriptor(
                    name = it.actionGroupKey,
                    providerInstance = it,
                    commands = toCommands(it)
                )
            }.plus(
                actionProviderContributions.map { ActionGroupDescriptor(
                    name = it.actionGroupKey,
                    providerInstance = it,
                    commands = toCommands(it)
                ) }
            )

    private val actionGroupDescriptorsMap: Map<String, ActionGroupDescriptor> =
        actionGroupDescriptors.associateBy { it.name }

    private val actionDescriptors: List<ActionCmdDescriptor> =
        actionGroupDescriptors.flatMap { it.commands }


    private fun toActionProviderInstance(property: KProperty1<ActionProviders, *>): ActionProvider<*> {
        return property.get(actionProviders) as ActionProvider<*>
    }

    private fun toCommands(actionProviderInstance:ActionProvider<*>): List<ActionCmdDescriptor> {

        val cmds = actionProviderInstance.findCommandClass()
            ?.sealedSubclasses
            ?.map { sealed -> buildApiCommandDescription(sealed, actionProviderInstance.actionGroupKey) }
            ?: emptyList()

        return cmds

    }

    /**
     * Builds a [ActionCmdDescriptor] based on a ModelCmd.
     *
     * At invocation time, commands are launched via the dispatch() method
     */
    private fun buildApiCommandDescription(sealed: KClass<out Any>, actionGroup: String): ActionCmdDescriptor {
        val doc = sealed.findAnnotation<ActionDoc>()
        return ActionCmdDescriptor(
            accessType = ActionCmdAccessType.DISPATCH,
            name = sealed.simpleName ?: "",
            group = actionGroup,
            title = doc?.title,
            description = doc?.description,
            resultType = typeOf<Unit>(),
            parameters = sealed.memberProperties.mapIndexed { index, property ->
                val paramdoc = property.findAnnotation<ActionParamDoc>()
                ActionCmdParamDescriptor(
                    name = property.name,
                    title = paramdoc?.name,
                    description = paramdoc?.description?.trimIndent(),
                    optional = property.returnType.isMarkedNullable,
                    type = property.returnType,
                    order = paramdoc?.order ?: index
                )
            },
            uiLocation = doc?.uiLocation ?: "",

        )
    }


    fun findAllGroupDescriptors(): Collection<ActionGroupDescriptor> {
        return actionGroupDescriptorsMap.values
    }

    fun findGroupDescriptorByIdOptional(actionGroup: String): ActionGroupDescriptor? {
        return actionGroupDescriptorsMap[actionGroup]
    }

    fun findAllActions(): Collection<ActionCmdDescriptor> {
        return actionDescriptors
    }


    fun handleInvocation(invocation: ActionRequest, actionCtx: ActionCtx): Any? {
        val actionGroup = invocation.group
        val actionCmd = invocation.command
        val actionPayload = invocation.payload


        val descriptor = findGroupDescriptorByIdOptional(actionGroup)
            ?: throw ActionInvocationException(
                HttpStatusCode.NotFound,
                "Unknown action group '$actionGroup'"
            )

        val actionProviderInstance: ActionProvider<Any> = descriptor.providerInstance as ActionProvider<Any>

        val commands = descriptor.commands.find { it.name == actionCmd }
            ?: throw ActionInvocationException(
                HttpStatusCode.NotFound,
                "Unknown function '$actionCmd' on '$actionGroup'"
            )

        val invoker: Invoker = when (commands.accessType) {
            ActionCmdAccessType.DISPATCH -> createInvokerDispatch(
                actionGroup, actionProviderInstance, actionCmd, actionPayload, actionCtx
            )
        }

        return try {
            invoker.invoke()
        } catch (e: InvocationTargetException) {
            val cause = e.cause
            if (cause != null) {
                logger.error("Invocation failed", e)
                throw ActionInvocationException(
                    HttpStatusCode.InternalServerError,
                    cause::class.simpleName ?: "Invocation failed",
                    mapOf(
                        "details" to (e.cause?.message ?: e::class.simpleName ?: e).toString()
                    )
                )
            } else {
                logger.error("Invocation failed", e)
                throw ActionInvocationException(
                    HttpStatusCode.InternalServerError,
                    "Invocation failed",
                    mapOf(
                        "details" to (e.message ?: e::class.simpleName).toString()
                    )
                )
            }
        } catch (throwable: Throwable) {
            logger.error("Invocation failed", throwable)
            throw ActionInvocationException(
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
        actionGroup: String,
        actionProviderInstance: ActionProvider<Any>,
        actionCmd: String,
        actionPayload: JsonObject,
        actionCtx: ActionCtx
    ): Invoker {

        val cls =
            actionProviderInstance.findCommandClass()?.sealedSubclasses?.firstOrNull { it.simpleName == actionCmd }
                ?: throw ActionInvocationException(
                    HttpStatusCode.NotFound,
                    "Command $actionCmd not found"
                )

        val function = cls.primaryConstructor ?: throw ActionInvocationException(
            HttpStatusCode.InternalServerError,
            "Command $actionCmd has no primary constructor"
        )

        val callArgs = createCallArgs(actionGroup, actionProviderInstance, function, actionPayload)
        logger.debug("call args: {}", callArgs)



        return object : Invoker {
            override fun invoke(): Any? {
                val cmd = function.callBy(callArgs)
                return actionProviderInstance.dispatch(cmd, actionCtx)
            }
        }
    }


    fun createCallArgs(
        actionGroup: String,
        actionProviderInstance: ActionProvider<Any>,
        actionCommandFunction: KFunction<*>,
        actionPayload: JsonObject
    ): MutableMap<KParameter, Any?> {
        val missing = actionCommandFunction.parameters
            .filter { it.kind == KParameter.Kind.VALUE && !it.isOptional && it.name !in actionPayload.keys }
            .mapNotNull { it.name }

        if (missing.isNotEmpty()) {
            throw ActionInvocationException(
                HttpStatusCode.BadRequest,
                "Missing parameter(s): ${missing.joinToString(", ")}",
                mapOf(
                    "usage" to buildUsageHint(actionGroup, actionCommandFunction)
                )
            )
        }

        val callArgs = mutableMapOf<KParameter, Any?>()
        val conversionErrors = mutableListOf<String>()

        actionCommandFunction.parameters.forEach { parameter ->
            when (parameter.kind) {
                KParameter.Kind.INSTANCE -> callArgs[parameter] = actionProviderInstance
                KParameter.Kind.VALUE -> {
                    val raw = parameter.name?.let(actionPayload::get)
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
            throw ActionInvocationException(
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
                when (val inner = convert(raw, innerParam.type.classifier)) {
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

    private fun buildUsageHint(actionGroup: String, function: KFunction<*>): String =
        listOf(actionGroup, function.name).joinToString(separator = " ") + " " + function.parameters
            .filter { it.kind == KParameter.Kind.VALUE }
            .joinToString(" ") { param -> "--${param.name}=<${param.type}>" }

    private sealed interface ConversionResult {
        data class Value(val value: Any?) : ConversionResult
        data class Error(val message: String) : ConversionResult
    }


    companion object {

        private val logger = LoggerFactory.getLogger(ActionRegistry::class.java)
    }
}