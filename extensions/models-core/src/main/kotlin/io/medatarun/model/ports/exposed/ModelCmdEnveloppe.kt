package io.medatarun.model.ports.exposed

import io.medatarun.actions.domain.ActionInstanceId
import io.medatarun.security.AppActorId
import io.medatarun.security.AppPrincipal

data class ModelCmdEnveloppe(
    val actionId: ActionInstanceId,
    val actorId: AppActorId,
    val cmd: ModelCmd,
)