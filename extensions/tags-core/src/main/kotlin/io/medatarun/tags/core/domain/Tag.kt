package io.medatarun.tags.core.domain

/**
 * Unified tag model used as migration target for both free tags and managed tags.
 *
 * A free tag is represented with a local [scope].
 * A managed tag is represented with the global [scope].
 */
interface Tag {
    /**
     * Tag unique identifier in application instance
     */
    val id: TagId

    /**
     * Business key of the tag.
     */
    val key: TagKey

    /**
     * Scope that owns the semantic meaning of the tag.
     */
    val scope: TagScope

    /**
     * Tells which group controls this tag, if any.
     */
    val groupId: TagGroupId?

    /**
     * Display name of the tag
     */
    val name: String?

    /**
     * Description of the tag
     */
    val description: String?

    /**
     * True when the tag is attached to a group and is therefore managed.
     */
    val isManaged: Boolean
        get() = scope.isManaged
}
