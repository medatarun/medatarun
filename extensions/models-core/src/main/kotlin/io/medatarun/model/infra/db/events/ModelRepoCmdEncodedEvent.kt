package io.medatarun.model.infra.db.events

data class ModelRepoCmdEncodedEvent(
    val eventType: String,
    val eventVersion: Int,
    val payload: String
)