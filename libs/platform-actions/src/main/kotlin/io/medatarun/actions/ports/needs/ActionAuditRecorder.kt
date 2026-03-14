package io.medatarun.actions.ports.needs

import io.medatarun.actions.domain.ActionInstanceId
import io.medatarun.security.AppPrincipalId

/**
 * Records the action lifecycle as seen from the action system.
 *
 * Contract:
 * - [recordReceived] is emitted when the action request reaches the action system.
 * - [recordRejected] is emitted when the action system rejects the request before business invocation.
 * - [recordSucceeded] is emitted when business invocation completes successfully.
 * - [recordFailed] is emitted when business invocation throws.
 */
interface ActionAuditRecorder {
    fun recordReceived(event: ActionAuditReceived)
    fun recordRejected(event: ActionAuditRejected)
    fun recordSucceeded(event: ActionAuditSucceeded)
    fun recordFailed(event: ActionAuditFailed)
}

data class ActionAuditReceived(
    val actionInstanceId: ActionInstanceId,
    val actionGroupKey: String,
    val actionKey: String,
    val principalId: AppPrincipalId?,
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
