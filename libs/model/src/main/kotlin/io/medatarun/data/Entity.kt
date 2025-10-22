package io.medatarun.data

import io.medatarun.model.model.AttributeDefId
import io.medatarun.model.model.EntityDefId

interface Entity {
    val id: EntityInstanceId
    val entityDefId: EntityDefId
    val attributes: Map<AttributeDefId, Any?>
}
