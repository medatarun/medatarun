package io.medatarun.ext.datamdfile.internal

import io.medatarun.data.Entity
import io.medatarun.data.EntityInstanceId
import io.medatarun.model.model.EntityDefId

data class MdEntity(
    override val id: EntityInstanceId,
    override val entityTypeId: EntityDefId,
    override val attributes: Map<String, Any>
) : Entity