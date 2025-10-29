package io.medatarun.resources

import io.medatarun.model.model.*

sealed interface ModelResourceCmd {

    // Models

    class Import(val from: String) : ModelResourceCmd

    // Relationships

    class CreateRelationshipDef(
        val modelId: ModelId,
        val initializer: RelationshipDef
    ) : ModelResourceCmd

    class UpdateRelationshipDef(
        val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val cmd: RelationshipDefUpdateCmd
    ) : ModelResourceCmd

    class DeleteRelationshipDef(
        val modelId: ModelId,
        val relationshipDefId: RelationshipDefId
    ) : ModelResourceCmd


    class CreateRelationshipAttributeDef(
        val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val attr: AttributeDef
    ) : ModelResourceCmd

    class UpdateRelationshipAttributeDef(
        val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val attributeDefId: AttributeDefId,
        val cmd: AttributeDefUpdateCmd
    ) : ModelResourceCmd

    class DeleteRelationshipAttributeDef(
        val modelId: ModelId,
        val relationshipDefId: RelationshipDefId,
        val attributeDefId: AttributeDefId
    ) : ModelResourceCmd



}