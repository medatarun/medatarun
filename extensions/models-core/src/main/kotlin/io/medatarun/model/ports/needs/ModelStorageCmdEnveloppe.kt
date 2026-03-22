package io.medatarun.model.ports.needs

import io.medatarun.security.AppTraceabilityRecord

/**
 * Storage envelope for repository commands.
 *
 * The storage layer needs the action traceability record to persist the event log
 * without depending on the action system types above this boundary.
 */
data class ModelStorageCmdEnveloppe(
    val traceabilityRecord: AppTraceabilityRecord,
    val cmd: ModelStorageCmd,
)
