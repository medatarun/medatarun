package io.medatarun.data

import io.medatarun.model.model.ModelEntityId

interface Entity {
    val id: EntityInstanceId
    val entityTypeId: ModelEntityId
    val attributes: Map<String, Any>
}
