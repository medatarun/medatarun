package io.medatarun.data.model

import io.medatarun.model.model.AttributeDefId
import io.medatarun.model.model.EntityDefId

interface Entity {
    val id: EntityId
    val entityDefId: EntityDefId
    val attributes: Map<AttributeDefId, Any?>
}
