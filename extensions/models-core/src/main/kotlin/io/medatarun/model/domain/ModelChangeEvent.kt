package io.medatarun.model.domain

import io.medatarun.security.AppTraceabilityRecord
import kotlinx.serialization.json.JsonObject
import java.time.Instant

/**
 * A change event in the model history
 */
interface ModelChangeEvent {
    /**
     * Each event has a unique identifier
     */
    val eventId: String

    /**
     * Type of event (model_release, model_update_name, etc.)
     */
    val eventType: String
    /**
     * Version number of the type of event (1, 2, 3). It allows determining the format of the payload
     */
    val eventVersion: Int

    /**
     * Event sequence number in the chain of events
     */
    val eventSequenceNumber: Int

    /**
     * Instant the event was created
     */
    val createdAt: Instant

    /**
     * Trace of the origin of the event (actor and origin)
     */
    val traceabilityRecord: AppTraceabilityRecord

    /**
     * Model version, present if the event is of `model_release` type
     */
    val modelVersion: ModelVersion?

    /**
     * Event payload
     */
    val payload: JsonObject
}
