package io.medatarun.model.ports.needs

import io.medatarun.actions.domain.ActionInstanceId
import io.medatarun.security.AppActorId

/**
 * Storage envelope for repository commands.
 *
 * The storage layer needs the action and actor identifiers to persist the event log
 * without depending on the action system types above this boundary.
 */
data class ModelStorageCmdEnveloppe(
    val actionId: ActionInstanceId,
    val principalId: AppActorId,
    val cmd: ModelStorageCmd,
)
