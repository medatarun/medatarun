package io.medatarun.model.domain

import io.medatarun.tags.core.domain.TagId

/**
 * A [Relationship] describes the conceptual structure of a link between entities.
 * It belongs to the *structural* layer of the metamodel: it defines what exists,
 * not what should happen. There is no lifecycle or business logic here.
 */
interface Relationship {
    /**
     * Unique identifier in the application instance and more generally across all instances since it is backed by UUID
     */
    val id: RelationshipId

    /**
     * Unique key of the relationship within the model.
     * Used for referencing from other definitions or instances.
     */
    val key: RelationshipKey

    /**
     * Human-readable name, optionally localized.
     * Purely descriptive; has no structural or behavioral effect.
     */
    val name: LocalizedText?

    /**
     * Optional textual description providing semantic context.
     * This is explanatory metadata, not executable logic.
     **/
    val description: LocalizedMarkdown?

    /**
     * The list of roles that participate in this relationship.
     * Each role defines one participating entity, the role’s name, and its cardinality.
     *
     * This structure supports relationships of any arity:
     *
     * - In a binary relationship, two entities are linked (e.g. Company employs Person).
     * - In an n-ary relationship, more than two entities are involved
     *   (e.g. Professor teaches Concept to StudentClass — three roles in one relationship).
     *
     * In other words, the relationship is not restricted to a single pair of entities;
     * it can represent a fact connecting several entities through well-defined roles.
     */
    val roles: List<RelationshipRole>

    /**
     * Tags added to relationship for classification
     */
    val tags: List<TagId>

    val ref get() = RelationshipRef.ById(id)
}

/**
 * A RelationshipRole describes how an entity participates in a relationship.
 * This element is strictly structural: it does not express ownership,
 * containment, or deletion rules.
 *
 * Lifecycle and business logic should be modeled separately
 * (for example via a LifecycleRule or Policy layer).
 */
interface RelationshipRole {
    /**
     * Unique identifier in the application instance and more generally across all instances since it is backed by UUID
     */
    val id: RelationshipRoleId

    /**
     * Unique identifier of the role within the relationship.
     *
     * Used for referencing from other definitions or instances.
     */
    val key: RelationshipRoleKey

    /**
     * Reference to the participating entity (defined in [Entity]).
     *
     * Note that you can use the same entityId in multiple roles of the same
     * relationship to create self-references.
     */
    val entityId: EntityId

    /**
     * Name of the role: expresses the function of the entity within the relationship
     * (e.g. "employer", "employee", "department").
     *
     * Semantic label only — has no behavioral meaning.
     */
    val name: LocalizedText?

    /**
     * Participation cardinality.
     *
     * Constrains how many instances of this entity can play this role in
     * a given relationship instance.
     *
     * This is a structural constraint, not an execution rule.
     */
    val cardinality: RelationshipCardinality


    val ref get() = RelationshipRoleRef.ById(id)
}
