package io.medatarun.actions.internal

import io.medatarun.actions.domain.ActionInvocationException
import io.medatarun.actions.domain.ActionInvoker
import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionRequest
import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.http.StatusCode
import io.medatarun.security.SecurityRuleEvaluatorResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.InvocationTargetException

internal class ActionInvokerImpl(
    val registry: ActionRegistryImpl,
    val actionTypesRegistry: ActionTypesRegistry,
    val actionSecurityRuleEvaluators: ActionSecurityRuleEvaluators
): ActionInvoker {

    override fun handleInvocation(invocation: ActionRequest, actionCtx: ActionCtx): Any? {

        val actionKey = invocation.actionKey
        val actionGroupKey = invocation.actionGroupKey
        val actionPayloadSerialized = invocation.payload


        // Find action, throws if not found
        val action = registry.findActionOptional(actionGroupKey, actionKey)
            ?: throw ActionInvocationException(StatusCode.NOT_FOUND, "Unknown action '$actionGroupKey/$actionKey'")

        // Evaluate security first, before any attempt to decode the payload
        val securityRuleEvaluationResult =
            actionSecurityRuleEvaluators.evaluateSecurity(action.descriptor.securityRule, actionCtx)
        if (securityRuleEvaluationResult is SecurityRuleEvaluatorResult.Error) {
            throw ActionInvocationException(
                StatusCode.UNAUTHORIZED,
                "Unauthorized",
                mapOf("details" to securityRuleEvaluationResult.msg)
            )
        }

        // Deserialize the payload if needed
        val deserializedPayload = registry.findDeserializer(action.descriptor.id)
            .deserialize(action, actionPayloadSerialized)

        // Find specialized invoker, depending on how the action is declared
        val invoker: ActionRegistryImpl.Invoker = registry.findInvoker(action.descriptor.id)

        // Invoke action, catch all exceptions and get the result
        val actionInvocationResult = try {
            invoker.invoke(deserializedPayload, actionCtx)
        } catch (e: InvocationTargetException) {
            val cause = e.cause
            if (cause != null) {
                logger.error("Invocation failed", e)
                throw ActionInvocationException(
                    StatusCode.INTERNAL_SERVER_ERROR,
                    cause::class.simpleName ?: "Invocation failed",
                    mapOf(
                        "details" to (e.cause?.message ?: e::class.simpleName ?: e).toString()
                    )
                )
            } else {
                logger.error("Invocation failed", e)
                throw ActionInvocationException(
                    StatusCode.INTERNAL_SERVER_ERROR,
                    "Invocation failed",
                    mapOf(
                        "details" to (e.message ?: e::class.simpleName).toString()
                    )
                )
            }
        } catch (e: MedatarunException) {
            if (e.httpStatusCode.httpStatusCode < StatusCode.INTERNAL_SERVER_ERROR.httpStatusCode) {
                throw ActionInvocationException(
                    e.httpStatusCode,
                    e.msg
                )
            } else {
                logger.error("Invocation failed", e)
                throw ActionInvocationException(
                    StatusCode.INTERNAL_SERVER_ERROR,
                    "Internal server error",
                    mapOf("details" to e.msg)
                )
            }
        } catch (throwable: Throwable) {
            logger.error("Invocation failed", throwable)
            throw ActionInvocationException(
                StatusCode.INTERNAL_SERVER_ERROR,
                "Invocation failed",
                mapOf(
                    "details" to (throwable.message ?: throwable::class.simpleName).toString()
                )
            )
        }
        return actionInvocationResult
    }


    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ActionInvokerImpl::class.java)
    }

}