package io.medatarun.model.model

import io.medatarun.model.ports.RepositoryRef

/**
 * Commands to change the model, entity definitions, entity definition's attributes definitions
 */
interface ModelCmds {

    // Model

    fun createModel(id: ModelId, name: LocalizedText, description: LocalizedMarkdown?, version: ModelVersion, repositoryRef: RepositoryRef = RepositoryRef.Auto)
    fun updateModelName(modelId: ModelId, name: LocalizedTextNotLocalized)
    fun updateModelDescription(modelId: ModelId, description: LocalizedTextNotLocalized?)
    fun updateModelVersion(modelId: ModelId, version: ModelVersion)
    fun deleteModel(modelId: ModelId)

    // Model -> Type

    fun createType(modelId: ModelId, initializer: ModelTypeInitializer)
    fun updateType(modelId: ModelId, typeId: ModelTypeId, cmd: ModelTypeUpdateCmd)
    fun deleteType(modelId: ModelId, typeId: ModelTypeId)

    // Model -> EntityDef

    fun createEntityDef(modelId: ModelId, entityDefInitializer: EntityDefInitializer)
    fun updateEntityDef(modelId: ModelId, entityDefId: EntityDefId, cmd: EntityDefUpdateCmd)
    fun deleteEntityDef(modelId: ModelId, entityDefId: EntityDefId)

    // Model -> EntityDef -> AttributeDef

    fun createEntityDefAttributeDef(modelId: ModelId, entityDefId: EntityDefId, attributeDefInitializer: AttributeDefInitializer)
    fun deleteEntityDefAttributeDef(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId)
    fun updateEntityDefAttributeDef(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId, cmd: AttributeDefUpdateCmd)

    // Model -> Relationship

    //@formatter:off
    fun createRelationshipDef(modelId: ModelId, initializer: RelationshipDef)
    fun updateRelationshipDef(modelId: ModelId, relationshipDefId: RelationshipDefId, cmd: RelationshipDefUpdateCmd)
    fun deleteRelationshipDef(modelId: ModelId, relationshipDefId: RelationshipDefId)
    //@formatter:on

    // Model -> Relationship -> AttributeDef

    //@formatter:off
    fun createRelationshipAttributeDef(modelId: ModelId, relationshipDefId: RelationshipDefId, attr: AttributeDef)
    fun updateRelationshipAttributeDef(modelId: ModelId, relationshipDefId: RelationshipDefId, attributeDefId: AttributeDefId, cmd: AttributeDefUpdateCmd)
    fun deleteRelationshipAttributeDef(modelId: ModelId, relationshipDefId: RelationshipDefId, attributeDefId: AttributeDefId)
    //@formatter:on

}
