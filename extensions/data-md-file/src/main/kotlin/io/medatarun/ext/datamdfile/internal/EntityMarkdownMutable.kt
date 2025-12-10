package io.medatarun.ext.datamdfile.internal

import io.medatarun.data.model.Entity
import io.medatarun.data.model.EntityId
import io.medatarun.model.domain.AttributeDefId
import io.medatarun.model.domain.EntityDefId

data class EntityMarkdownMutable(
    override val id: EntityId,
    override val entityDefId: EntityDefId,
    override val attributes: MutableMap<AttributeDefId, Any?>
) : Entity