package io.medatarun.tags.core.domain

import io.medatarun.type.commons.text.TextMarkdown
import io.medatarun.type.commons.text.TextSingleLine

/**
 * Unified tag model used as migration target for both local tags and global tags.
 *
 * A local tag is represented with a local [scope].
 * A global tag is represented with the global [scope].
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
    val scope: TagScopeRef

    /**
     * Tells which group controls this tag, if any.
     */
    val groupId: TagGroupId?

    /**
     * Display name of the tag
     */
    val name: TextSingleLine?

    /**
     * Description of the tag
     */
    val description: TextMarkdown?

    /**
     * True when the tag is attached to a group and is therefore a global tag.
     */
    val isGlobal: Boolean
        get() = scope.isGlobal

    /**
     * Stable reference to the tag.
     *
     * Using the identifier avoids breaking links when the tag key changes.
     */
    val ref: TagRef
        get() = TagRef.ById(id)
}
