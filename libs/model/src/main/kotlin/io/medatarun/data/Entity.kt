package io.medatarun.data

import io.medatarun.model.model.EntityDefId

interface Entity {
    val id: EntityInstanceId
    val entityTypeId: EntityDefId
    val attributes: Map<String, Any>
}
