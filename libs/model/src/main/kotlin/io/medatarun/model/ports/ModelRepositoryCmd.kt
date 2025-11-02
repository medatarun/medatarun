package io.medatarun.model.ports

import io.medatarun.model.infra.EntityDefInMemory
import io.medatarun.model.model.*

sealed interface ModelRepositoryCmdWithId: ModelRepositoryCmd {
    val modelId: ModelId
}
sealed interface ModelRepositoryCmd {


    // ------------------------------------------------------------------------
    // Models
    // ------------------------------------------------------------------------

    data class UpdateModelName(
        override val modelId: ModelId,
        val name: LocalizedTextNotLocalized
    ) : ModelRepositoryCmdWithId

    data class UpdateModelDescription(
        override val modelId: ModelId,
        val description: LocalizedTextNotLocalized?
    ) : ModelRepositoryCmdWithId

    data class UpdateModelVersion(
        override val modelId: ModelId,
        val version: ModelVersion
    ) : ModelRepositoryCmdWithId

    data class DeleteModel(
        override val modelId: ModelId
    ) : ModelRepositoryCmdWithId

    // ------------------------------------------------------------------------
    // Types
    // ------------------------------------------------------------------------

    data class CreateType(
        override val modelId: ModelId,
        val initializer: ModelTypeInitializer
    ) : ModelRepositoryCmdWithId

    data class UpdateType(
        override val modelId: ModelId,
        val typeId: ModelTypeId,
        val cmd: ModelTypeUpdateCmd
    ) : ModelRepositoryCmdWithId

    data class DeleteType(
        override val modelId: ModelId,
        val typeId: ModelTypeId
    ) : ModelRepositoryCmdWithId

    // ------------------------------------------------------------------------
    // Entity
    // ------------------------------------------------------------------------

    data class CreateEntityDef(
        override val modelId: ModelId,
        val entityDef: EntityDefInMemory
    ) : ModelRepositoryCmdWithId

    data class UpdateEntityDef(
        override val modelId: ModelId,
        val entityDefId: EntityDefId,
        val cmd: EntityDefUpdateCmd
    ) : ModelRepositoryCmdWithId

    data class DeleteEntityDef(
        override val modelId: ModelId,
        val entityDefId: EntityDefId
    ) : ModelRepositoryCmdWithId

    // ------------------------------------------------------------------------
    // Entity attributes
    // ------------------------------------------------------------------------

    class CreateEntityDefAttributeDef(
        override val modelId: ModelId,
        val entityDefId: EntityDefId,
        val attributeDef: AttributeDef
    ) : ModelRepositoryCmdWithId

    class DeleteEntityDefAttributeDef(
        override val modelId: ModelId,
        val entityDefId: EntityDefId,
        val attributeDefId: AttributeDefId
    ) : ModelRepositoryCmdWithId

    class UpdateEntityDefAttributeDef(
        override val modelId: ModelId,
        val entityDefId: EntityDefId,
        val attributeDefId: AttributeDefId,
        val cmd: AttributeDefUpdateCmd
    ) : ModelRepositoryCmdWithId

    // ------------------------------------------------------------------------
    // Relationships
    // ------------------------------------------------------------------------

    class CreateRelationshipDef(
        override val modelId: ModelId,
        val initializer: RelationshipDef
    ) : ModelRepositoryCmdWithId

    class UpdateRelationshipDef(
        override val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val cmd: RelationshipDefUpdateCmd
    ) : ModelRepositoryCmdWithId

    class DeleteRelationshipDef(
        override val modelId: ModelId,
        val relationshipDefId: RelationshipDefId
    ) : ModelRepositoryCmdWithId


    class CreateRelationshipAttributeDef(
        override val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val attr: AttributeDef
    ) : ModelRepositoryCmdWithId

    class UpdateRelationshipAttributeDef(
        override val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val attributeDefId: AttributeDefId,
        val cmd: AttributeDefUpdateCmd
    ) : ModelRepositoryCmdWithId

    class DeleteRelationshipAttributeDef(
        override val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val attributeDefId: AttributeDefId
    ) : ModelRepositoryCmdWithId


}