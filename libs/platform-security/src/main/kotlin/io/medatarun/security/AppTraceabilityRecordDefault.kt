package io.medatarun.security

/**
 * Default variant of traceability record. Typically used when you read such
 * records from storage.
 */
data class AppTraceabilityRecordDefault(
    override val origin: String,
    override val actorId: AppActorId
) : AppTraceabilityRecord
