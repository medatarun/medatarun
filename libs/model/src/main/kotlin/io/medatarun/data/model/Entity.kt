package io.medatarun.data.model

import io.medatarun.model.domain.AttributeDefId
import io.medatarun.model.domain.EntityDefId

interface Entity {
    val id: EntityId
    val entityDefId: EntityDefId
    val attributes: Map<AttributeDefId, Any?>
}
