package io.medatarun.model.ports.exposed

import io.medatarun.security.AppTraceabilityRecord

data class ModelCmdEnveloppe(
    val traceabilityRecord: AppTraceabilityRecord,
    val cmd: ModelCmd,
)
