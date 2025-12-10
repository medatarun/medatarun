package io.medatarun.model.domain

import java.net.URL

@JvmInline value class ModelKey(val value: String) {
    fun validated(): ModelKey {
        return this
    }
}

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
    val id: ModelKey
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
     * Origin of the model, either created by the application or imported from another source
     */
    val origin: ModelOrigin

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

    /**
     * Hashtags used as classifiers for the model
     */
    val hashtags: List<Hashtag>

    fun findTypeOptional(typeId: TypeKey): ModelType? = types.firstOrNull { it.id == typeId }
    fun findType(typeId: TypeKey): ModelType = findTypeOptional(typeId) ?: throw TypeNotFoundException(this.id, typeId)
    fun ensureTypeExists(typeId: TypeKey): ModelType = findType(typeId)

    /**
     * Returns entity definition by its id or null
     */
    fun findEntityDefOptional(id: EntityKey): EntityDef? = entityDefs.firstOrNull { it.id == id }
    /**
     * Returns entity definition by its id or throw [EntityDefNotFoundException]
     */
    fun findEntityDef(id: EntityKey): EntityDef = findEntityDefOptional(id) ?: throw EntityDefNotFoundException(this@Model.id, id)

    /**
     * Returns relationship definition by its id
     */
    fun findRelationshipDefOptional(id: RelationshipKey): RelationshipDef? = relationshipDefs.firstOrNull { it.id == id }

    /**
     * Returns relationship definition by its id or throw [RelationshipDefNotFoundException]
     */
    fun findRelationshipDef(id: RelationshipKey): RelationshipDef = findRelationshipDefOptional(id) ?: throw RelationshipDefNotFoundException(this@Model.id, id)

    /**
     * Syntax sugar to check if a relationship exists
     */
    fun ensureRelationshipExists(relationshipKey: RelationshipKey) = findRelationshipDef(relationshipKey)

}


