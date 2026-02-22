package io.medatarun.tags.core.domain

/**
 * A free tag is a tag that is not managed by the company who operates the application. It means its form is free.
 */
interface TagFree {
    /**
     * Tag unique identifier in application instance
     */
    val id: TagFreeId

    /**
     * Business key of the tag.
     */
    val key: TagFreeKey

    /**
     * Display name of the tag
     */
    val name: String?

    /**
     * Description of the tag
     */
    val description: String?
}
