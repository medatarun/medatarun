package io.medatarun.model.infra.db.events

/**
 * Base data of a serialized event. JSON is encoded.
 */
data class ModelEventEncoded(
    /**
     * Event type
     */
    val eventType: String,
    /**
     * Event version
     */
    val eventVersion: Int,
    /**
     * Event payload, which is JSON serialized into a String
     */
    val payload: String
)