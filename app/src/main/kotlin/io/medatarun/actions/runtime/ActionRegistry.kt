package io.medatarun.actions.runtime

import io.ktor.http.*
import io.medatarun.actions.ports.needs.*
import io.medatarun.kernel.ExtensionRegistry
import io.medatarun.security.SecurityRuleEvaluatorResult
import kotlinx.serialization.json.JsonObject
import org.slf4j.LoggerFactory
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.typeOf

class ActionRegistry(private val extensionRegistry: ExtensionRegistry) {

    private val actionTypesRegistry = ActionTypesRegistry(extensionRegistry)
    private val actionSecurityRegistry = ActionSecurityRegistry(extensionRegistry)
    private val actionParametersParser = ActionParametersParser(actionTypesRegistry)
    private val actionProviderContributions = extensionRegistry.findContributionsFlat(ActionProvider::class)

    private val actionGroupDescriptors: List<ActionGroupDescriptor> =
        actionProviderContributions.map {
            ActionGroupDescriptor(
                key = it.actionGroupKey,
                providerInstance = it,
                actions = toActions(it)
            )
        }

    private val actionGroupDescriptorsMap: Map<String, ActionGroupDescriptor> =
        actionGroupDescriptors.associateBy { it.key }

    private val actionDescriptors: List<ActionCmdDescriptor> =
        actionGroupDescriptors.flatMap { it.actions }




    private fun toActions(actionProviderInstance: ActionProvider<*>): List<ActionCmdDescriptor> {

        val cmds = actionProviderInstance.findCommandClass()
            ?.sealedSubclasses
            ?.map { sealed -> buildActionsDescriptions(sealed, actionProviderInstance.actionGroupKey) }
            ?: emptyList()

        return cmds

    }

    /**
     * Builds a [ActionCmdDescriptor] based on a ModelCmd.
     *
     * At invocation time, commands are launched via the dispatch() method
     */
    private fun buildActionsDescriptions(sealed: KClass<out Any>, actionGroup: String): ActionCmdDescriptor {
        val doc = sealed.findAnnotation<ActionDoc>() ?: throw ActionDefinitionWithoutDocException(
            actionGroup,
            sealed.simpleName ?: "unknown"
        )

        // Checks that all security rules are resolved
        val securityRule = doc.securityRule
        actionSecurityRegistry.findEvaluatorOptional(securityRule)
            ?: throw ActionDefinitionWithUnknownSecurityRule(actionGroup, doc.key, securityRule)

        return ActionCmdDescriptor(
            accessType = ActionCmdAccessType.DISPATCH,
            key = doc.key,
            actionClassName = sealed.simpleName ?: "",
            group = actionGroup,
            title = doc.title,
            description = doc.description,
            resultType = typeOf<Unit>(),
            parameters = sealed.memberProperties.mapIndexed { index, property ->
                val paramdoc = property.findAnnotation<ActionParamDoc>()
                ActionCmdParamDescriptor(
                    name = property.name,
                    title = paramdoc?.name,
                    description = paramdoc?.description?.trimIndent(),
                    optional = property.returnType.isMarkedNullable,
                    type = property.returnType,
                    multiplatformType = actionTypesRegistry.toMultiplatformType(property.returnType),
                    jsonType = actionTypesRegistry.toJsonType(property.returnType),
                    order = paramdoc?.order ?: index
                )
            },
            uiLocation = doc.uiLocation ?: "",
            securityRule = securityRule

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
        val actionKey = invocation.actionKey
        val actionGroupKey = invocation.actionGroupKey
        val actionPayload = invocation.payload


        val descriptor = findGroupDescriptorByIdOptional(actionGroupKey)
            ?: throw ActionInvocationException(
                HttpStatusCode.NotFound,
                "Unknown action group '$actionGroupKey'"
            )

        val actionProviderInstance: ActionProvider<Any> = descriptor.providerInstance as ActionProvider<Any>

        val actionDescriptor = descriptor.actions.find { it.key == actionKey }
            ?: throw ActionInvocationException(
                HttpStatusCode.NotFound,
                "Unknown action '$actionGroupKey/$actionKey'"
            )

        val securityRuleEvaluationResult = actionSecurityRegistry.evaluateSecurity(actionDescriptor.securityRule, actionCtx)
        if (securityRuleEvaluationResult is SecurityRuleEvaluatorResult.Error) {
            throw ActionInvocationException(
                HttpStatusCode.Unauthorized,
                "Unauthorized",
                mapOf("details" to securityRuleEvaluationResult.msg))
        }

        val invoker: Invoker = when (actionDescriptor.accessType) {
            ActionCmdAccessType.DISPATCH -> createInvokerDispatch(
                actionDescriptor = actionDescriptor,
                actionProviderInstance = actionProviderInstance,
                actionPayload = actionPayload,
                actionCtx = actionCtx
            )
        }

        val actionInvocationResult = try {
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
        return actionInvocationResult
    }

    interface Invoker {
        fun invoke(): Any?
    }

    private fun createInvokerDispatch(
        actionDescriptor: ActionCmdDescriptor,
        actionProviderInstance: ActionProvider<Any>,
        actionPayload: JsonObject,
        actionCtx: ActionCtx
    ): Invoker {
        val actionGroupKey = actionDescriptor.group
        val actionKey = actionDescriptor.key
        val cls =
            actionProviderInstance.findCommandClass()
                ?.sealedSubclasses
                ?.firstOrNull { it.simpleName == actionDescriptor.actionClassName }
                ?: throw ActionInvocationException(
                    HttpStatusCode.NotFound,
                    "Action $actionGroupKey/$actionKey not found"
                )

        val function = cls.primaryConstructor ?: throw ActionInvocationException(
            HttpStatusCode.InternalServerError,
            "Action $actionGroupKey/$actionKey has no primary constructor"
        )

        val callArgs = createCallArgs(actionGroupKey, actionKey, actionProviderInstance, function, actionPayload)
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
        actionCmd: String,
        actionProviderInstance: ActionProvider<Any>,
        actionCommandFunction: KFunction<*>,
        actionPayload: JsonObject
    ): Map<KParameter, Any?> {
        val missing = actionCommandFunction.parameters
            .filter { it.kind == KParameter.Kind.VALUE && !it.isOptional && it.name !in actionPayload.keys }
            .mapNotNull { it.name }

        if (missing.isNotEmpty()) {
            throw ActionInvocationException(
                HttpStatusCode.BadRequest,
                "Missing parameter(s): ${missing.joinToString(", ")}",
                mapOf(
                    "usage" to buildUsageHint(actionGroup, actionCmd, actionCommandFunction)
                )
            )
        }

        val params = actionParametersParser.buildCallArgs(
            actionCommandFunction = actionCommandFunction,
            actionProviderInstance = actionProviderInstance,
            actionPayload = actionPayload,
        )


        if (params.conversionErrors.isNotEmpty()) {
            throw ActionInvocationException(
                HttpStatusCode.BadRequest,
                "Invalid parameter values",
                mapOf(
                    "details" to params.conversionErrors.joinToString(", ")
                )
            )
        }
        return params.callArgs
    }



    private fun buildUsageHint(actionGroup: String, actionCmd: String, function: KFunction<*>): String =
        listOf(actionGroup, actionCmd).joinToString(separator = " ") + " " + function.parameters
            .filter { it.kind == KParameter.Kind.VALUE }
            .joinToString(" ") { param -> "--${param.name}=<${param.type}>" }



    companion object {

        private val logger = LoggerFactory.getLogger(ActionRegistry::class.java)
    }
}