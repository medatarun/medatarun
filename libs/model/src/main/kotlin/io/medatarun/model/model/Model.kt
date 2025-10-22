package io.medatarun.model.model

@JvmInline value class ModelId(val value: String)
@JvmInline value class ModelLocation(val value: String)
@JvmInline value class ModelVersion(val value: String)

/**
 * A model contains multiple [EntityDef] that contains [AttributeDef].
 *
 * Think of it as a Domain Model in DDD in a bounded context
 */
interface Model {
    /**
     * Unique identifier of the model accros all models managed by the current application instance
     */
    val id: ModelId
    /**
     * Display name of the model
     */
    val name: LocalizedText?
    /**
     * Display description of the model
     */
    val description: LocalizedMarkdown?
    /**
     * Version of the model
     */
    val version: ModelVersion

    /**
     * Lists types known by the model
     */
    val types: List<ModelType>

    /**
     * Entity definitions in this model
     */
    val entityDefs: List<EntityDef>

    /**
     * Returns entity definition by its id or null
     */
    fun findEntityDefOptional(entityDefId: EntityDefId): EntityDef? = entityDefs.firstOrNull { it.id == entityDefId }
    /**
     * Returns entity definition by its id or throw [EntityDefNotFoundException]
     */
    fun findEntityDef(entityDefId: EntityDefId): EntityDef = findEntityDefOptional(entityDefId) ?: throw EntityDefNotFoundException(id, entityDefId)
}


