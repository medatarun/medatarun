package io.medatarun.model.domain

import io.medatarun.tags.core.domain.TagId

/**
 * Attribute definition for an [Entity]
 */
interface Attribute {


    /**
     * Unique identifier in the application instance and more generally across all instances since it is backed by UUID
     */
    val id: AttributeId

    /**
     * Tells if attribute is owned by an entity or a relationship and its id
     */
    val ownerId: AttributeOwnerId

    /**
     * Unique key of the attribute in its [Entity]
     */
    val key: AttributeKey

    /**
     * Display name of the attribute
     */
    val name: TextSingleLine?

    /**
     * Display description of the attribute
     */
    val description: TextMarkdown?

    /**
     * Type of attribute, must be one of the types registered in the model
     */
    val typeId: TypeId

    /**
     * Indicates that this attribute is optional in Entities (default is that attributes are required).
     */
    val optional: Boolean

    /**
     * Tags added to this attribute for categorization
     */
    val tags: List<TagId>

    fun ownedBy(id: EntityId): Boolean {
        val ownerIdSafe = ownerId
        return ownerIdSafe is AttributeOwnerId.OwnerEntityId && ownerIdSafe.id == id
    }
    fun ownedBy(id: RelationshipId): Boolean {
        val ownerIdSafe = ownerId
        return ownerIdSafe is AttributeOwnerId.OwnerRelationshipId && ownerIdSafe.id == id
    }
}
