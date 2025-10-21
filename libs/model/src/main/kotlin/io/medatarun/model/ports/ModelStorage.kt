package io.medatarun.model.ports

import io.medatarun.model.model.LocalizedMarkdown
import io.medatarun.model.model.LocalizedText
import io.medatarun.model.model.Model
import io.medatarun.model.model.AttributeDef
import io.medatarun.model.model.AttributeDefId
import io.medatarun.model.model.EntityDef
import io.medatarun.model.model.EntityDefId
import io.medatarun.model.model.ModelId
import io.medatarun.model.model.ModelTypeId

interface ModelStorage {

    // Models

    fun findModelById(id: ModelId): Model
    fun createModel(mode: Model)
    fun deleteModel(modelId: ModelId)
    fun findAllModelIds(): List<ModelId>

    // Models -> EntityDef

    fun createEntityDef(modelId: ModelId, e: EntityDef)
    fun updateEntityDefId(modelId: ModelId, entityDefId: EntityDefId, newEntityId: EntityDefId)
    fun updateEntityDefName(modelId: ModelId, entityDefId: EntityDefId, title: LocalizedText?)
    fun updateEntityDefDescription(modelId: ModelId, entityDefId: EntityDefId, description: LocalizedMarkdown?)
    fun deleteEntityDef(modelId: ModelId, entityDefId: EntityDefId)

    // Model -> EntityDef -> AttributeDef

    fun createEntityDefAttributeDef(modelId: ModelId, entityDefId: EntityDefId, attr: AttributeDef)
    fun updateEntityDefAttributeDefId(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId, newAttributeDefId: AttributeDefId)
    fun updateEntityDefAttributeDefName(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId, name: LocalizedText?)
    fun updateEntityDefAttributeDefDescription(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId, description: LocalizedMarkdown?)
    fun updateEntityDefAttributeDefType(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId, type: ModelTypeId)
    fun updateEntityDefAttributeDefOptional(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId, optional: Boolean)
    fun deleteEntityDefAttributeDef(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId)
}
