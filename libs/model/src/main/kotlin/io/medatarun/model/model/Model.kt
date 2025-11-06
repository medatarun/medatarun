package io.medatarun.model.model

import java.net.URL

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

    /**
     * Documentation home
     */
    val documentationHome: URL?

    fun findTypeOptional(typeId: ModelTypeId): ModelType? = types.firstOrNull { it.id == typeId }
    fun findType(typeId: ModelTypeId): ModelType = findTypeOptional(typeId) ?: throw TypeNotFoundException(this.id, typeId)
    fun ensureTypeExists(typeId: ModelTypeId): ModelType = findType(typeId)

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

    /**
     * Returns relationship definition by its id or throw [RelationshipDefNotFoundException]
     */
    fun findRelationshipDef(id: RelationshipDefId): RelationshipDef = findRelationshipDefOptional(id) ?: throw RelationshipDefNotFoundException(this@Model.id, id)

    /**
     * Syntax sugar to check if a relationship exists
     */
    fun ensureRelationshipExists(relationshipDefId: RelationshipDefId) = findRelationshipDef(relationshipDefId)

}


