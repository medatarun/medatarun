package io.medatarun.model.ports

import io.medatarun.model.infra.EntityDefInMemory
import io.medatarun.model.model.AttributeDef
import io.medatarun.model.model.AttributeDefId
import io.medatarun.model.model.AttributeDefInitializer
import io.medatarun.model.model.AttributeDefUpdateCmd
import io.medatarun.model.model.EntityDefId
import io.medatarun.model.model.EntityDefInitializer
import io.medatarun.model.model.EntityDefUpdateCmd
import io.medatarun.model.model.ModelCmd
import io.medatarun.model.model.ModelId
import io.medatarun.model.model.RelationshipDef
import io.medatarun.model.model.RelationshipDefId
import io.medatarun.model.model.RelationshipDefUpdateCmd

sealed interface ModelRepositoryCmd {
    val modelId: ModelId

    // ------------------------------------------------------------------------
    // Entity
    // ------------------------------------------------------------------------

    data class CreateEntityDef(
        override val modelId: ModelId,
        val entityDef: EntityDefInMemory
    ) : ModelRepositoryCmd

    data class UpdateEntityDef(
        override val modelId: ModelId,
        val entityDefId: EntityDefId,
        val cmd: EntityDefUpdateCmd
    ) : ModelRepositoryCmd

    data class DeleteEntityDef(
        override val modelId: ModelId,
        val entityDefId: EntityDefId
    ) : ModelRepositoryCmd

    // ------------------------------------------------------------------------
    // Entity attributes
    // ------------------------------------------------------------------------

    class CreateEntityDefAttributeDef(
        override val modelId: ModelId,
        val entityDefId: EntityDefId,
        val attributeDef: AttributeDef
    ): ModelRepositoryCmd

    class DeleteEntityDefAttributeDef(
        override val modelId: ModelId,
        val entityDefId: EntityDefId,
        val attributeDefId: AttributeDefId
    ): ModelRepositoryCmd

    class UpdateEntityDefAttributeDef(
        override val modelId: ModelId,
        val entityDefId: EntityDefId,
        val attributeDefId: AttributeDefId,
        val cmd: AttributeDefUpdateCmd
    ): ModelRepositoryCmd

    // ------------------------------------------------------------------------
    // Relationships
    // ------------------------------------------------------------------------

    class CreateRelationshipDef(
        override val modelId: ModelId,
        val initializer: RelationshipDef
    ) : ModelRepositoryCmd

    class UpdateRelationshipDef(
        override val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val cmd: RelationshipDefUpdateCmd
    ) : ModelRepositoryCmd

    class DeleteRelationshipDef(
        override val modelId: ModelId,
        val relationshipDefId: RelationshipDefId
    ) : ModelRepositoryCmd


    class CreateRelationshipAttributeDef(
        override val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val attr: AttributeDef
    ) : ModelRepositoryCmd

    class UpdateRelationshipAttributeDef(
        override val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val attributeDefId: AttributeDefId,
        val cmd: AttributeDefUpdateCmd
    ) : ModelRepositoryCmd

    class DeleteRelationshipAttributeDef(
        override val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val attributeDefId: AttributeDefId
    ) : ModelRepositoryCmd


}