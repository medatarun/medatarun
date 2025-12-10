package io.medatarun.data.adapters

import io.medatarun.data.domain.EntityId

data class EntityIdString(
    private val value: String
) : EntityId {
    override fun asString(): String = value
}