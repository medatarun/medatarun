package io.medatarun.tags.core.internal

import io.medatarun.tags.core.domain.TagLocalCommandIncompatibleTagScopeRefException
import io.medatarun.tags.core.domain.TagScopeManagerNotFoundException
import io.medatarun.tags.core.domain.TagScopeRef

interface TagScopes {
    /**
     * Checks if the specified scope is local and exists by delegating to local scope managers.
     *
     * Contract: just returns if ok or throws exceptions
     * - if scope is not local [TagLocalCommandIncompatibleTagScopeRefException]
     * - if no manager for this scope type exist [TagScopeManagerNotFoundException]
     * - if scope doesn't exist [io.medatarun.tags.core.domain.TagScopeNotFoundException]
     */
    fun ensureLocalScopeExists(ref: TagScopeRef)
}