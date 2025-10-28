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
     * Relationship definitions in this model
     */
    val relationshipDefs: List<RelationshipDef>

    fun findTypeOptional(id: ModelTypeId): ModelType? = types.firstOrNull { it.id == id }

    /**
     * Returns entity definition by its id or null
     */
    fun findEntityDefOptional(id: EntityDefId): EntityDef? = entityDefs.firstOrNull { it.id == id }
    /**
     * Returns entity definition by its id or throw [EntityDefNotFoundException]
     */
    fun findEntityDef(id: EntityDefId): EntityDef = findEntityDefOptional(id) ?: throw EntityDefNotFoundException(this@Model.id, id)

    /**
     * Returns relationship definition by its id
     */
    fun findRelationshipDefOptional(id: RelationshipDefId): RelationshipDef? = relationshipDefs.firstOrNull { it.id == id }

}


