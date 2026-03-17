package io.medatarun.model.domain

import io.medatarun.actions.domain.ActionInstanceId
import io.medatarun.security.AppPrincipalId
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
     * Original action that made the event
     */
    val actionId: ActionInstanceId

    /**
     * Model version, present if the event is of `model_release` type
     */
    val modelVersion: ModelVersion?

    /**
     * Application principal identifier (actor) that made this event
     */
    val principalId: AppPrincipalId

    /**
     * Event payload
     */
    val payload: JsonObject
}
