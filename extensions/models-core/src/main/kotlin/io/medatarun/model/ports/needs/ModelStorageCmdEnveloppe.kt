package io.medatarun.model.ports.needs

import io.medatarun.actions.domain.ActionInstanceId
import io.medatarun.security.AppPrincipalId

/**
 * Storage envelope for repository commands.
 *
 * The storage layer needs the action and actor identifiers to persist the event log
 * without depending on the action system types above this boundary.
 */
data class ModelStorageCmdEnveloppe(
    val actionId: ActionInstanceId,
    val principalId: AppPrincipalId,
    val cmd: ModelStorageCmd,
)
