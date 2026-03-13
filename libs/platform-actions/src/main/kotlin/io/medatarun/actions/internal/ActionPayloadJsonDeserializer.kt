package io.medatarun.actions.internal

import io.medatarun.actions.domain.ActionRegistered
import io.medatarun.actions.ports.needs.ActionPayload

internal interface ActionPayloadJsonDeserializer {
    fun deserialize(action: ActionRegistered, payload: ActionPayload): Any
}
