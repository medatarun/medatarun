package io.medatarun.actions.internal

import io.medatarun.actions.ports.needs.ActionPayload

interface ActionPayloadJsonDeserializer {
    fun deserialize(action: ActionRegistered, payload: ActionPayload): Any
}
