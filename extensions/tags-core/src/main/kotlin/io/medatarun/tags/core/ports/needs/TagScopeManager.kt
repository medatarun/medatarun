package io.medatarun.tags.core.ports.needs

import io.medatarun.tags.core.domain.TagId
import io.medatarun.tags.core.domain.TagScopeType

/**
 * Handles tag-related policies and reactions for a given local scope type.
 * Implementations live in consumer modules (or test fixtures) and can veto deletions.
 */
interface TagScopeManager {
    val type: TagScopeType

    /**
     * Called before a tag is deleted. Implementations may throw to veto the deletion.
     */
    fun onBeforeTagDelete(tagId: TagId)
}
