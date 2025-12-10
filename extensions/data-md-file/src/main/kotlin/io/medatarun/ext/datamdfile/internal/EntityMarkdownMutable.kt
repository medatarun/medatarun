package io.medatarun.ext.datamdfile.internal

import io.medatarun.data.domain.Entity
import io.medatarun.data.domain.EntityId
import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.EntityKey

data class EntityMarkdownMutable(
    override val id: EntityId,
    override val entityKey: EntityKey,
    override val attributes: MutableMap<AttributeKey, Any?>
) : Entity