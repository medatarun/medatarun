package io.medatarun.tags.core.ports.needs

import io.medatarun.tags.core.domain.TagId
import io.medatarun.tags.core.domain.TagScopeRef
import io.medatarun.tags.core.domain.TagScopeType

/**
 * Handles tag-related policies and reactions for a given local scope type.
 * Implementations live in consumer modules (or test fixtures) and can veto deletions.
 */
interface TagScopeManager {
    val type: TagScopeType

    /**
     * Ensures a local scope exists before a tag is created in it or a tag is managed
     * relatively to this scope
     */
    fun localScopeExists(scopeRef: TagScopeRef.Local): Boolean

    /**
     * Called before a tag is deleted. Implementations may throw to veto the deletion.
     */
    fun onBeforeTagDelete(tagId: TagId)
}
