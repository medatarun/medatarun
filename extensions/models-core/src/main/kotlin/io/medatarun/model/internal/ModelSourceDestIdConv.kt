package io.medatarun.model.internal

import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.RelationshipId
import io.medatarun.model.domain.EntityRef
import io.medatarun.model.domain.EntityAttributeRef
import io.medatarun.model.domain.RelationshipRef
import io.medatarun.model.domain.RelationshipAttributeRef

/**
 * Resolves destination refs from source ids when tags are applied on copied/imported models.
 */
interface ModelSourceDestIdConv {
    fun getDestEntityRef(sourceId: EntityId): EntityRef
    fun getDestRelationshipRef(sourceId: RelationshipId): RelationshipRef
    fun getDestEntityAttributeRef(sourceId: AttributeId): EntityAttributeRef
    fun getDestRelationshipAttributeRef(sourceId: AttributeId): RelationshipAttributeRef
}
