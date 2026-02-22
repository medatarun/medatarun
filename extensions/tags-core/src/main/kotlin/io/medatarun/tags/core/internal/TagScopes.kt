package io.medatarun.tags.core.internal

import io.medatarun.tags.core.domain.TagScopeRef

interface TagScopes {
    fun ensureLocalScopeExists(ref: TagScopeRef)
}