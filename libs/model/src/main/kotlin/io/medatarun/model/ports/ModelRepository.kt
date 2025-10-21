package io.medatarun.model.ports

import io.medatarun.model.model.*

/**
 * Model repository allow accessing and modifying stored models.
 *
 * This is the one extensions should implement to offer model storage capabilities.
 *
 * Do not confuse it with [ModelStorages] that acts as a composite around all [ModelRepository]
 *
 * [ModelStorages] contains multiple [ModelRepository]
 *
 */
interface ModelRepository {

    /**
     * Returns true if the repository matches this id.
     *
     * Why we do this like that: because a repository may have sub repositories (or not).
     * Repository ids can have subpaths, allowing one repository to spread its content into
     * multiple locations (or aggregate them).
     *
     * By not just checking the "id" of the repository but asking if this id matches him,
     * we get a clear answer if we should go with it or not (when creating models for example).
     */
    fun matchesId(id: ModelRepositoryId): Boolean

    // Manage models -> entity def

    fun findAllModelIds(): List<ModelId>
    fun findModelByIdOptional(id: ModelId): Model?
    fun createModel(model: Model)
    fun updateModelName(modelId: ModelId, name: LocalizedTextNotLocalized)
    fun updateModelDescription(modelId: ModelId, description: LocalizedTextNotLocalized?)
    fun updateModelVersion(modelId: ModelId, version: ModelVersion)
    fun deleteModel(modelId: ModelId)

    // Models -> EntityDef

    //@formatter:off
    fun createEntityDef(modelId: ModelId, e: EntityDef)
    fun updateEntityDefId(modelId: ModelId, entityDefId: EntityDefId, newEntityDefId: EntityDefId)
    fun updateEntityDefName(modelId: ModelId, entityDefId: EntityDefId, name: LocalizedText?)
    fun updateEntityDefDescription(modelId: ModelId, entityDefId: EntityDefId, description: LocalizedMarkdown?)
    fun deleteEntityDef(modelId: ModelId, entityDefId: EntityDefId)
    //@formatter:on

    // Model -> EntityDef -> AttributeDef

    //@formatter:off
    fun createEntityDefAttributeDef(modelId: ModelId, entityDefId: EntityDefId, attr: AttributeDef)
    fun updateEntityDefAttributeDefId(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId, newAttributeDefId: AttributeDefId)
    fun updateEntityDefAttributeDefName(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId, name: LocalizedText?)
    fun updateEntityDefAttributeDefDescription(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId, description: LocalizedMarkdown?)
    fun updateEntityDefAttributeDefType(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId, type: ModelTypeId)
    fun updateEntityDefAttributeDefOptional(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId, optional: Boolean)
    fun deleteEntityDefAttributeDef(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId)
    //@formatter:on
}