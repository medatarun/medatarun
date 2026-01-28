package io.medatarun.model.domain

import java.net.URL

/**
 * A model contains multiple [Entity] that contains [Attribute].
 *
 * Think of it as a Domain Model in DDD in a bounded context
 */
interface Model {

    /**
     * Unique identifier in the application instance and more generally across all instances since it is backed by UUID
     */
    val id: ModelId

    /**
     * Unique key of the model accros all models managed by the current application instance
     */
    val key: ModelKey

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
    val entities: List<Entity>

    /**
     * Relationship definitions in this model
     */
    val relationships: List<Relationship>

    /**
     * Documentation home
     */
    val documentationHome: URL?

    /**
     * Hashtags used as classifiers for the model
     */
    val hashtags: List<Hashtag>

    fun findTypeOptional(typeKey: TypeKey): ModelType? = types.firstOrNull { it.key == typeKey }
    fun findTypeOptional(typeId: TypeId): ModelType? = types.firstOrNull { it.id == typeId }
    fun findTypeOptional(typeRef: TypeRef): ModelType? {
        return when (typeRef) {
            is TypeRef.ById -> findTypeOptional(typeRef.id)
            is TypeRef.ByKey -> findTypeOptional(typeRef.key)
        }
    }

    fun findType(typeKey: TypeKey): ModelType =
        findTypeOptional(typeKey) ?: throw TypeNotFoundException(ModelRef.ById(this.id), TypeRef.ByKey(typeKey))

    fun findType(typeId: TypeId): ModelType =
        findTypeOptional(typeId) ?: throw TypeNotFoundException(ModelRef.ById(this.id), TypeRef.ById(typeId))

    fun findType(typeRef: TypeRef): ModelType =
        when (typeRef) {
            is TypeRef.ById -> findType(typeRef.id)
            is TypeRef.ByKey -> findType(typeRef.key)
        }


    /**
     * Returns entity definition by its id or null
     */
    fun findEntityOptional(id: EntityKey): Entity? = entities.firstOrNull { it.key == id }
    fun findEntityOptional(id: EntityId): Entity? = entities.firstOrNull { it.id == id }
    fun findEntityOptional(ref: EntityRef): Entity? = when (ref) {
        is EntityRef.ById -> findEntityOptional(ref.id)
        is EntityRef.ByKey -> findEntityOptional(ref.key)
    }

    fun findEntity(key: EntityKey): Entity = findEntityOptional(key)
        ?: throw EntityNotFoundException(ModelRef.ById(this.id), EntityRef.ByKey(key))

    fun findEntity(id: EntityId): Entity = findEntityOptional(id)
        ?: throw EntityNotFoundException(ModelRef.ById(this.id), EntityRef.ById(id))

    fun findEntity(ref: EntityRef): Entity = findEntityOptional(ref)
        ?: throw EntityNotFoundException(ModelRef.ById(this.id), ref)


    fun findEntityAttributeOptional(
        entityRef: EntityRef,
        attrKey: AttributeKey,
    ): Attribute? =
        findEntityOptional(entityRef)?.attributes?.firstOrNull { it.key == attrKey }

    fun findEntityAttributeOptional(
        entityRef: EntityRef,
        attrId: AttributeId,
    ): Attribute? =
        findEntityOptional(entityRef)?.attributes?.firstOrNull { it.id == attrId }

    fun findEntityAttributeOptional(
        entityRef: EntityRef,
        attrRef: EntityAttributeRef
    ): Attribute? = when (attrRef) {
        is EntityAttributeRef.ById -> findEntityAttributeOptional(entityRef, attrRef.id)
        is EntityAttributeRef.ByKey -> findEntityAttributeOptional(entityRef, attrRef.key)
    }

    fun findEntityAttribute(
        entityRef: EntityRef,
        attrRef: EntityAttributeRef
    ): Attribute = findEntityAttributeOptional(entityRef, attrRef)
        ?: throw EntityAttributeNotFoundException(ModelRef.ById(this.id), entityRef, attrRef)

    /**
     * Returns relationship definition by its id
     */
    fun findRelationshipOptional(key: RelationshipKey): Relationship? =
        relationships.firstOrNull { it.key == key }

    fun findRelationshipOptional(id: RelationshipId): Relationship? =
        relationships.firstOrNull { it.id == id }

    fun findRelationshipOptional(ref: RelationshipRef): Relationship? = when (ref) {
        is RelationshipRef.ById -> findRelationshipOptional(ref.id)
        is RelationshipRef.ByKey -> findRelationshipOptional(ref.key)
    }

    fun findRelationship(ref: RelationshipRef) = findRelationshipOptional(ref)
        ?: throw RelationshipNotFoundException(ModelRef.ById(this.id), ref)

    fun findRelationshipRoleOptional(
        relationshipRef: RelationshipRef,
        roleKey: RelationshipRoleKey
    ): RelationshipRole? =
        findRelationshipOptional(relationshipRef)?.roles?.firstOrNull { it.key == roleKey }

    fun findRelationshipRoleOptional(
        relationshipRef: RelationshipRef,
        roleId: RelationshipRoleId
    ): RelationshipRole? =
        findRelationshipOptional(relationshipRef)?.roles?.firstOrNull { it.id == roleId }

    fun findRelationshipRoleOptional(
        relationshipRef: RelationshipRef,
        roleRef: RelationshipRoleRef
    ): RelationshipRole? = when (roleRef) {
        is RelationshipRoleRef.ById -> findRelationshipRoleOptional(relationshipRef, roleRef.id)
        is RelationshipRoleRef.ByKey -> findRelationshipRoleOptional(relationshipRef, roleRef.key)
    }

    fun findRelationshipAttributeOptional(
        relationshipRef: RelationshipRef,
        attrKey: AttributeKey,
    ): Attribute? =
        findRelationshipOptional(relationshipRef)?.attributes?.firstOrNull { it.key == attrKey }

    fun findRelationshipAttributeOptional(
        relationshipRef: RelationshipRef,
        attrId: AttributeId,
    ): Attribute? =
        findRelationshipOptional(relationshipRef)?.attributes?.firstOrNull { it.id == attrId }

    fun findRelationshipAttributeOptional(
        relationshipRef: RelationshipRef,
        attrRef: RelationshipAttributeRef
    ): Attribute? = when (attrRef) {
        is RelationshipAttributeRef.ById -> findRelationshipAttributeOptional(relationshipRef, attrRef.id)
        is RelationshipAttributeRef.ByKey -> findRelationshipAttributeOptional(relationshipRef, attrRef.key)
    }

    fun findEntityAttributes(ref: EntityRef): List<Attribute> {
        return findEntity(ref).attributes
    }
    fun findRelationshipAttributes(ref: RelationshipRef): List<Attribute> {
        return findRelationship(ref).attributes
    }

}

