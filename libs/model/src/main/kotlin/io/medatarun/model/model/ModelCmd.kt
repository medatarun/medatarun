package io.medatarun.model.model

sealed interface ModelCmd {
    val modelId: ModelId

    // ------------------------------------------------------------------------
    // Types
    // ------------------------------------------------------------------------

    data class CreateType(override val modelId: ModelId, val initializer: ModelTypeInitializer): ModelCmd
    data class UpdateType(override val modelId: ModelId, val typeId: ModelTypeId, val cmd: ModelTypeUpdateCmd): ModelCmd
    data class DeleteType(override val modelId: ModelId, val typeId: ModelTypeId): ModelCmd

    // ------------------------------------------------------------------------
    // Entity
    // ------------------------------------------------------------------------

    data class CreateEntityDef(
        override val modelId: ModelId,
        val entityDefInitializer: EntityDefInitializer
    ) : ModelCmd

    data class UpdateEntityDef(
        override val modelId: ModelId,
        val entityDefId: EntityDefId,
        val cmd: EntityDefUpdateCmd
    ) : ModelCmd

    data class DeleteEntityDef(
        override val modelId: ModelId,
        val entityDefId: EntityDefId
    ) : ModelCmd

    // ------------------------------------------------------------------------
    // Entity attributes
    // ------------------------------------------------------------------------

    class CreateEntityDefAttributeDef(
        override val modelId: ModelId,
        val entityDefId: EntityDefId,
        val attributeDefInitializer: AttributeDefInitializer
    ) : ModelCmd

    class DeleteEntityDefAttributeDef(
        override val modelId: ModelId,
        val entityDefId: EntityDefId,
        val attributeDefId: AttributeDefId
    ) : ModelCmd

    class UpdateEntityDefAttributeDef(
        override val modelId: ModelId,
        val entityDefId: EntityDefId,
        val attributeDefId: AttributeDefId,
        val cmd: AttributeDefUpdateCmd
    ) : ModelCmd

    // ------------------------------------------------------------------------
    // Relationships
    // ------------------------------------------------------------------------

    class CreateRelationshipDef(
        override val modelId: ModelId,
        val initializer: RelationshipDef
    ) : ModelCmd

    class UpdateRelationshipDef(
        override val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val cmd: RelationshipDefUpdateCmd
    ) : ModelCmd

    class DeleteRelationshipDef(
        override val modelId: ModelId,
        val relationshipDefId: RelationshipDefId
    ) : ModelCmd


    class CreateRelationshipAttributeDef(
        override val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val attr: AttributeDef
    ) : ModelCmd

    class UpdateRelationshipAttributeDef(
        override val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val attributeDefId: AttributeDefId,
        val cmd: AttributeDefUpdateCmd
    ) : ModelCmd

    class DeleteRelationshipAttributeDef(
        override val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val attributeDefId: AttributeDefId
    ) : ModelCmd


}