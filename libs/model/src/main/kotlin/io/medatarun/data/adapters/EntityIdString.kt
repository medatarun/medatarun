package io.medatarun.data.adapters

import io.medatarun.data.model.EntityId

data class EntityIdString(
    private val value: String
) : EntityId {
    override fun asString(): String = value
}