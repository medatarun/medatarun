package io.medatarun.ext.datamdfile.internal

import io.medatarun.data.Entity
import io.medatarun.data.EntityInstanceId
import io.medatarun.model.model.AttributeDefId
import io.medatarun.model.model.EntityDefId

data class EntityMarkdownMutable(
    override val id: EntityInstanceId,
    override val entityDefId: EntityDefId,
    override val attributes: MutableMap<AttributeDefId, Any?>
) : Entity