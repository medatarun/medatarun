package io.medatarun.tags.core.domain

/**
 * Group of global tags, e.g. a category. It helps grouping tags related to the
 * same subjects and provide a way to control the tag forms.
 *
 * For example, you could create a "location" tag group with countries, a "security"
 * tag group with security levels or "GDPR" tag group to classify data privacy needs.
 *
 * For now, groups can only contain predefined tags.
 */
interface TagGroup {
    /**
     * Unique group identifier
     */
    val id: TagGroupId

    /**
     * Unique key of the group
     */
    val key: TagGroupKey

    /**
     * Clear tag group name
     */
    val name: String?

    /**
     * Description of the group, its usage and meaning
     */
    val description: String?
}