package io.medatarun.model.ports

import io.medatarun.model.infra.EntityDefInMemory
import io.medatarun.model.model.*

sealed interface ModelRepositoryCmdOnModel: ModelRepositoryCmd {
    val modelId: ModelId
}
sealed interface ModelRepositoryCmd {


    // ------------------------------------------------------------------------
    // Models
    // ------------------------------------------------------------------------

    data class CreateModel(
        val model: Model
    ): ModelRepositoryCmd

    data class UpdateModelName(
        override val modelId: ModelId,
        val name: LocalizedTextNotLocalized
    ) : ModelRepositoryCmdOnModel

    data class UpdateModelDescription(
        override val modelId: ModelId,
        val description: LocalizedTextNotLocalized?
    ) : ModelRepositoryCmdOnModel

    data class UpdateModelVersion(
        override val modelId: ModelId,
        val version: ModelVersion
    ) : ModelRepositoryCmdOnModel

    data class DeleteModel(
        override val modelId: ModelId
    ) : ModelRepositoryCmdOnModel

    // ------------------------------------------------------------------------
    // Types
    // ------------------------------------------------------------------------

    data class CreateType(
        override val modelId: ModelId,
        val initializer: ModelTypeInitializer
    ) : ModelRepositoryCmdOnModel

    data class UpdateType(
        override val modelId: ModelId,
        val typeId: ModelTypeId,
        val cmd: ModelTypeUpdateCmd
    ) : ModelRepositoryCmdOnModel

    data class DeleteType(
        override val modelId: ModelId,
        val typeId: ModelTypeId
    ) : ModelRepositoryCmdOnModel

    // ------------------------------------------------------------------------
    // Entity
    // ------------------------------------------------------------------------

    data class CreateEntityDef(
        override val modelId: ModelId,
        val entityDef: EntityDefInMemory
    ) : ModelRepositoryCmdOnModel

    data class UpdateEntityDef(
        override val modelId: ModelId,
        val entityDefId: EntityDefId,
        val cmd: EntityDefUpdateCmd
    ) : ModelRepositoryCmdOnModel

    data class DeleteEntityDef(
        override val modelId: ModelId,
        val entityDefId: EntityDefId
    ) : ModelRepositoryCmdOnModel

    // ------------------------------------------------------------------------
    // Entity attributes
    // ------------------------------------------------------------------------

    class CreateEntityDefAttributeDef(
        override val modelId: ModelId,
        val entityDefId: EntityDefId,
        val attributeDef: AttributeDef
    ) : ModelRepositoryCmdOnModel

    class DeleteEntityDefAttributeDef(
        override val modelId: ModelId,
        val entityDefId: EntityDefId,
        val attributeDefId: AttributeDefId
    ) : ModelRepositoryCmdOnModel

    class UpdateEntityDefAttributeDef(
        override val modelId: ModelId,
        val entityDefId: EntityDefId,
        val attributeDefId: AttributeDefId,
        val cmd: AttributeDefUpdateCmd
    ) : ModelRepositoryCmdOnModel

    // ------------------------------------------------------------------------
    // Relationships
    // ------------------------------------------------------------------------

    class CreateRelationshipDef(
        override val modelId: ModelId,
        val initializer: RelationshipDef
    ) : ModelRepositoryCmdOnModel

    class UpdateRelationshipDef(
        override val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val cmd: RelationshipDefUpdateCmd
    ) : ModelRepositoryCmdOnModel

    class DeleteRelationshipDef(
        override val modelId: ModelId,
        val relationshipDefId: RelationshipDefId
    ) : ModelRepositoryCmdOnModel


    class CreateRelationshipAttributeDef(
        override val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val attr: AttributeDef
    ) : ModelRepositoryCmdOnModel

    class UpdateRelationshipAttributeDef(
        override val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val attributeDefId: AttributeDefId,
        val cmd: AttributeDefUpdateCmd
    ) : ModelRepositoryCmdOnModel

    class DeleteRelationshipAttributeDef(
        override val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val attributeDefId: AttributeDefId
    ) : ModelRepositoryCmdOnModel


}