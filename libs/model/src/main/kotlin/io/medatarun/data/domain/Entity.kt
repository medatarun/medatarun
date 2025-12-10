package io.medatarun.data.domain

import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.EntityKey

interface Entity {
    val id: EntityId
    val entityKey: EntityKey
    val attributes: Map<AttributeKey, Any?>
}
