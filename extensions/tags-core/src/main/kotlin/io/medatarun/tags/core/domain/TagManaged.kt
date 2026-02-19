package io.medatarun.tags.core.domain

interface TagManaged {
    /**
     * Tag unique identifier in application instance
     */
    val id: TagManagedId

    /**
     * Business key of the tag.
     */
    val key: TagManagedKey

    /**
     * Tells which group controls this tag
     */
    val groupId: TagGroupId

    /**
     * Display name of the tag
     */
    val name: String?

    /**
     * Description of the tag
     */
    val description: String?
}