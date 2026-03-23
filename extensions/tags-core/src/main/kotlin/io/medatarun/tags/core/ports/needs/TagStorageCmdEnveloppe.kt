package io.medatarun.tags.core.ports.needs

import io.medatarun.security.AppTraceabilityRecord

/**
 * Storage envelope for tag repository commands.
 *
 * The storage layer needs the traceability record to persist or publish command effects
 * without depending on the action layer above this boundary.
 */
data class TagStorageCmdEnveloppe(
    val traceabilityRecord: AppTraceabilityRecord,
    val cmd: TagStorageCmd,
)
