package io.medatarun.actions.internal

import io.medatarun.actions.domain.*
import io.medatarun.actions.ports.needs.*
import io.medatarun.platform.telemetry.Telemetry
import io.medatarun.security.SecurityRuleEvaluatorResult
import io.medatarun.type.commons.id.Id

internal class ActionInvokerImpl(
    val registry: ActionRegistryImpl,
    val actionSecurityRuleEvaluators: ActionSecurityRuleEvaluators,
    private val actionAuditRecorder: ActionAuditRecorder,
    private val telemetry: Telemetry
) : ActionInvoker {

    override fun handleInvocation(invocation: ActionRequest, actionRequestCtx: ActionRequestCtx): Any? {
        return telemetry.span("action ${invocation.actionGroupKey}/${invocation.actionKey}") { span ->
            span.setAttribute("medatarun.action.key", invocation.actionGroupKey+"/"+invocation.actionKey)
            span.setAttribute("medatarun.action.source", actionRequestCtx.source)
            handleInvocationTelemetry(invocation, actionRequestCtx)
        }
    }
    private fun handleInvocationTelemetry(invocation: ActionRequest, actionRequestCtx: ActionRequestCtx): Any? {


        val actionInstanceId = Id.generate(::ActionInstanceId)



        // Record that the action request reached the action system.
        actionAuditRecorder.onActionReceived(
            ActionAuditReceived(
                actionInstanceId = actionInstanceId,
                actionGroupKey = invocation.actionGroupKey,
                actionKey = invocation.actionKey,
                actorId = actionRequestCtx.principalCtx.principal?.id,
                actorDisplayName = actionRequestCtx.principalCtx.principal?.fullname,
                payloadSerialized = serializePayload(invocation.payload),
                source = actionRequestCtx.source
            )
        )

        try {
            val actionInvocationResult = handleInvocationInternal(invocation, actionRequestCtx, actionInstanceId)
            // At this point the action has been accepted and business invocation completed.
            actionAuditRecorder.onActionSucceeded(ActionAuditSucceeded(actionInstanceId))
            return actionInvocationResult
        } catch (exception: ActionInvocationException) {
            // ActionInvocationException belongs to the action system: the request was rejected before business invoke.
            actionAuditRecorder.onActionRejected(
                ActionAuditRejected(
                    actionInstanceId = actionInstanceId,
                    code = exception.status.name,
                    message = exception.message ?: exception.status.message
                )
            )
            throw exception
        } catch (exception: Exception) {
            // Any other exception comes from business invocation or a deeper technical failure during invoke.
            actionAuditRecorder.onActionFailed(
                ActionAuditFailed(
                    actionInstanceId = actionInstanceId,
                    code = exception::class.simpleName ?: Exception::class.simpleName.toString(),
                    message = exception.message ?: ""
                )
            )
            throw exception
        }
    }

    private fun handleInvocationInternal(
        invocation: ActionRequest,
        actionRequestCtx: ActionRequestCtx,
        actionInstanceId: ActionInstanceId
    ): Any? {
        val actionKey = invocation.actionKey
        val actionGroupKey = invocation.actionGroupKey
        val actionPayloadSerialized = invocation.payload

        // Find action, throws if not found
        val action = registry.findActionOptional(actionGroupKey, actionKey)
            ?: throw ActionInvocationNotFoundException(actionGroupKey, actionKey)

        // Evaluate security first, before any attempt to decode the payload
        val securityRuleEvaluationResult =
            actionSecurityRuleEvaluators.evaluateSecurity(action.descriptor.securityRule, actionRequestCtx)
        if (securityRuleEvaluationResult is SecurityRuleEvaluatorResult.AuthenticationError) {
            throw ActionInvocationUnauthorizedException(securityRuleEvaluationResult.msg)
        }
        if (securityRuleEvaluationResult is SecurityRuleEvaluatorResult.AuthorizationError) {
            throw ActionInvocationForbiddenException(securityRuleEvaluationResult.msg)
        }

        // Deserialize the payload if needed
        val deserializedPayload = registry.findDeserializer(action.descriptor.id)
            .deserialize(action, actionPayloadSerialized)

        // Find specialized invoker, depending on how the action is declared
        val specializedInvoker: ActionRegistryImpl.Invoker = registry.findInvoker(action.descriptor.id)

        // Transform the request context into a context suitable for action handlers
        val actionCtx = object : ActionCtx {
            override fun dispatchAction(req: ActionRequest): Any? {
                return handleInvocation(req, actionRequestCtx)
            }

            override val requestCtx: ActionRequestCtx
                get() = actionRequestCtx

            override val actionInstanceId: ActionInstanceId
                get() = actionInstanceId

            override val principal: ActionPrincipalCtx
                get() = actionRequestCtx.principalCtx

        }

        // Invoke business action
        return specializedInvoker.invoke(deserializedPayload, actionCtx)
    }

    override fun evaluateSecurity(
        actionGroupKey: String,
        actionKey: String,
        actionRequestCtx: ActionRequestCtx
    ): Boolean {
        val action = registry.findActionOptional(actionGroupKey, actionKey)
            ?: throw ActionInvocationNotFoundException(actionGroupKey, actionKey)
        val securityRuleEvaluationResult =
            actionSecurityRuleEvaluators.evaluateSecurity(action.descriptor.securityRule, actionRequestCtx)
        return securityRuleEvaluationResult is SecurityRuleEvaluatorResult.Ok
    }

    private fun serializePayload(payload: ActionPayload): String {
        return when (payload) {
            is ActionPayload.AsJson -> payload.value.toString()
            is ActionPayload.AsRaw -> payload.value.toString()
        }
    }

}
