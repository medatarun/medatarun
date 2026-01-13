package io.medatarun.actions.runtime

import io.ktor.http.*
import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.actions.ports.needs.ActionRequest
import io.medatarun.kernel.ExtensionRegistry
import io.medatarun.security.SecurityRuleEvaluatorResult
import io.medatarun.types.TypeDescriptor
import kotlinx.serialization.json.JsonObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.full.primaryConstructor

class ActionInvoker(
    val registry: ActionRegistry,
    val extensionRegistry: ExtensionRegistry,
    val actionSecurityRuleEvaluators: ActionSecurityRuleEvaluators
) {

    private val actionTypesRegistry = ActionTypesRegistry(
        extensionRegistry.findContributionsFlat(TypeDescriptor::class)
    )
    private val actionParamBinder = ActionParamBinder(actionTypesRegistry)


    fun handleInvocation(invocation: ActionRequest, actionCtx: ActionCtx): Any? {
        val actionKey = invocation.actionKey
        val actionGroupKey = invocation.actionGroupKey
        val actionPayload = invocation.payload


        val descriptor = registry.findGroupDescriptorByIdOptional(actionGroupKey)
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

        val securityRuleEvaluationResult =
            actionSecurityRuleEvaluators.evaluateSecurity(actionDescriptor.securityRule, actionCtx)
        if (securityRuleEvaluationResult is SecurityRuleEvaluatorResult.Error) {
            throw ActionInvocationException(
                HttpStatusCode.Unauthorized,
                "Unauthorized",
                mapOf("details" to securityRuleEvaluationResult.msg)
            )
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
        val actionClass = actionProviderInstance.findCommandClass()
            ?.sealedSubclasses
            ?.firstOrNull { it.simpleName == actionDescriptor.actionClassName }
            ?: throw ActionInvocationException(
                HttpStatusCode.NotFound,
                "Action $actionGroupKey/$actionKey not found"
            )

        val bindings = actionParamBinder.buildConstructorArgs(
            actionClass = actionClass,
            actionProviderInstance = actionProviderInstance,
            actionPayload = actionPayload,
        )

        // This will throw exceptions if invalid parameters exists (missing, with errors, etc.)
        bindings.technicalValidation()

        // Now we can assume there is no error anymore on parameters
        logger.debug("call args: {}", bindings)

        return object : Invoker {
            override fun invoke(): Any? {
                val cmd = actionClass.primaryConstructor?.callBy(bindings.toCallArgs())
                    ?: throw ActionInvocationException(
                        HttpStatusCode.InternalServerError,
                        "Action class $actionClass has no primary constructor"
                    )
                return actionProviderInstance.dispatch(cmd, actionCtx)
            }
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ActionInvoker::class.java)
    }

}