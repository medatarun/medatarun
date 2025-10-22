package io.medatarun.ext.datamdfile.internal

import io.medatarun.data.Entity
import io.medatarun.data.EntityInstanceId
import io.medatarun.model.model.AttributeDefId
import io.medatarun.model.model.EntityDefId

data class MdEntityMutable(
    override val id: EntityInstanceId,
    override val entityTypeId: EntityDefId,
    override val attributes: MutableMap<AttributeDefId, Any?>
) : Entity