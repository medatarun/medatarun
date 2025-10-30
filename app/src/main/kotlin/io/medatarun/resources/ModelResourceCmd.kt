package io.medatarun.resources

import io.medatarun.model.model.*

sealed interface ModelResourceCmd {

    // Models


    class Import(val from: String) : ModelResourceCmd

    @ResourceCommandDoc(
        title = "Inspect models",
        description = "Produces a tree view of registered models, entities, and attributes in the runtime."
    )
    class Inspect() : ModelResourceCmd

    @ResourceCommandDoc(
        title = "Inspect models (JSON)",
        description = "Returns the registered models, entities, and attributes with all metadata encoded as JSON. Preferred method for AI agents to understand the model."
    )
    class InspectJson() : ModelResourceCmd

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