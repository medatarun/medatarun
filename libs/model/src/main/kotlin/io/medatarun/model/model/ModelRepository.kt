package io.medatarun.model.model

interface ModelRepository {

    // Manage models -> entity def

    fun findAllModelIds(): List<ModelId>
    fun findModelByIdOptional(id: ModelId): Model?
    fun createModel(model: Model)
    fun deleteModel(modelId: ModelId)

    // Manage model -> entity def

    fun createEntityDef(modelId: ModelId, e: EntityDef)
    fun updateEntityDefId(modelId: ModelId, entityDefId: EntityDefId, newEntityDefId: EntityDefId)
    fun updateEntityDefTitle(modelId: ModelId, entityDefId: EntityDefId, title: LocalizedText?)
    fun updateEntityDefDescription(modelId: ModelId, entityDefId: EntityDefId, description: LocalizedMarkdown?)
    fun deleteEntityDef(modelId: ModelId, entityDefId: EntityDefId)

    // Manage model -> entity def -> attribute def

    fun createEntityDefAttributeDef(modelId: ModelId, entityDefId: EntityDefId, attr: AttributeDef)
    fun updateEntityDefAttributeDefName(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId, newAttributeId: AttributeDefId)
    fun updateEntityDefAttributeDefTitle(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId, title: LocalizedText?)
    fun updateEntityDefAttributeDefDescription(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId, description: LocalizedMarkdown?)
    fun updateEntityDefAttributeDefType(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId, type: ModelTypeId)
    fun updateEntityDefAttributeDefOptional(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId, optional: Boolean)
    fun deleteEntityDefAttributeDef(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId)
}
