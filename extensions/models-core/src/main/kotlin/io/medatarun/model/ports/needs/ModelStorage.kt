package io.medatarun.model.ports.needs

import io.medatarun.model.domain.Attribute
import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.Entity
import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.Model
import io.medatarun.model.domain.ModelAggregate
import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelType
import io.medatarun.model.domain.Relationship
import io.medatarun.model.domain.RelationshipId
import io.medatarun.model.domain.RelationshipKey
import io.medatarun.model.domain.TypeId
import io.medatarun.model.domain.TypeKey
import io.medatarun.model.domain.search.SearchResults

/**
 * Model storage allows accessing and modifying stored models.
 *
 * The current runtime uses one native SQL storage selected by the model extension itself.
 */
interface ModelStorage {

    fun findAllModelIds(): List<ModelId>

    fun existsModelByKey(key: ModelKey): Boolean

    fun existsModelById(id: ModelId): Boolean

    fun findModelByKeyOptional(key: ModelKey): Model?

    fun findModelByIdOptional(id: ModelId): Model?

    fun findModelAggregateByKeyOptional(key: ModelKey): ModelAggregate?

    fun findModelAggregateByIdOptional(id: ModelId): ModelAggregate?

    fun findTypeByKeyOptional(modelId: ModelId, key: TypeKey): ModelType?

    fun findTypeByIdOptional(modelId: ModelId, typeId: TypeId): ModelType?

    fun findEntityByIdOptional(modelId: ModelId, entityId: EntityId): Entity?

    fun findEntityByKeyOptional(modelId: ModelId, entityKey: EntityKey): Entity?

    fun findEntityAttributeByIdOptional(modelId: ModelId, entityId: EntityId, attributeId: AttributeId): Attribute?

    fun findEntityAttributeByKeyOptional(modelId: ModelId, entityId: EntityId, key: AttributeKey): Attribute?

    fun findRelationshipByIdOptional(modelId: ModelId, relationshipId: RelationshipId): Relationship?

    fun findRelationshipByKeyOptional(modelId: ModelId, relationshipKey: RelationshipKey): Relationship?

    fun findRelationshipAttributeByKeyOptional(modelId: ModelId, relationshipId: RelationshipId, key: AttributeKey): Attribute?



    fun search(query: ModelStorageSearchQuery): SearchResults

    fun isTypeUsedInEntityAttributes(modelId: ModelId, typeId: TypeId): Boolean
    fun isTypeUsedInRelationshipAttributes(modelId: ModelId, typeId: TypeId): Boolean

    // Commands

    /**
     * Process one model storage command.
     *
     * See [ModelRepoCmd] for the list of supported write operations.
     */
    fun dispatch(cmd: ModelRepoCmd)



}
