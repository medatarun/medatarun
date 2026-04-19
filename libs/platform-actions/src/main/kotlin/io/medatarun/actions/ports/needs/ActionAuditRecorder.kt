package io.medatarun.actions.ports.needs

import io.medatarun.actions.domain.ActionInstanceId
import io.medatarun.platform.kernel.ServiceContributionPoint
import io.medatarun.security.AppActorId

/**
 * Records the action lifecycle as seen from the action system.
 *
 * Contract:
 * - [onActionReceived] is emitted when the action request reaches the action system.
 * - [onActionRejected] is emitted when the action system rejects the request before business invocation.
 * - [onActionSucceeded] is emitted when business invocation completes successfully.
 * - [onActionFailed] is emitted when business invocation throws.
 */
interface ActionAuditRecorder: ServiceContributionPoint {
    fun onActionReceived(event: ActionAuditReceived)
    fun onActionRejected(event: ActionAuditRejected)
    fun onActionSucceeded(event: ActionAuditSucceeded)
    fun onActionFailed(event: ActionAuditFailed)
}

data class ActionAuditReceived(
    val actionInstanceId: ActionInstanceId,
    val actionGroupKey: String,
    val actionKey: String,
    val actorId: AppActorId?,
    val actorDisplayName: String?,
    val payloadSerialized: String,
    val source: String
)

data class ActionAuditRejected(
    val actionInstanceId: ActionInstanceId,
    val code: String,
    val message: String
)

data class ActionAuditSucceeded(
    val actionInstanceId: ActionInstanceId
)

data class ActionAuditFailed(
    val actionInstanceId: ActionInstanceId,
    val code: String,
    val message: String
)
