package io.medatarun.ext.datamdfile.internal

import io.medatarun.data.model.Entity
import io.medatarun.data.model.EntityId
import io.medatarun.model.model.AttributeDefId
import io.medatarun.model.model.EntityDefId

data class EntityMarkdownMutable(
    override val id: EntityId,
    override val entityDefId: EntityDefId,
    override val attributes: MutableMap<AttributeDefId, Any?>
) : Entity