package io.medatarun.tags.core.ports.needs

import io.medatarun.tags.core.domain.TagScopeRef
import io.medatarun.tags.core.domain.TagScopeType

/**
 * Handles tag-related policies and reactions for a given local scope type.
 * Implementations live in consumer modules (or test fixtures) and can veto deletions.
 */
interface TagScopeManager {
    val type: TagScopeType

    /**
     * Ensures a local scope exists before a tag is created inside it or before a tag is processed in operations
     * relative to this scope
     */
    fun localScopeExists(scopeRef: TagScopeRef.Local): Boolean


}
