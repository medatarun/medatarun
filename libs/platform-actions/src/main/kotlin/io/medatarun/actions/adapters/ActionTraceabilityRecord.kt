package io.medatarun.actions.adapters

import io.medatarun.actions.domain.ActionInstanceId
import io.medatarun.security.AppActorId
import io.medatarun.security.AppTraceabilityRecord

data class ActionTraceabilityRecord(
    val actionInstanceId: ActionInstanceId,
    override val actorId: AppActorId,
) : AppTraceabilityRecord {
    override val origin: String
        get() = "action:" + actionInstanceId.asString()
}