package io.medatarun.tags.core.internal

import io.medatarun.tags.core.domain.TagFreeCommandIncompatibleTagScopeRefException
import io.medatarun.tags.core.domain.TagScopeManagerNotFoundException
import io.medatarun.tags.core.domain.TagScopeNotFoundException
import io.medatarun.tags.core.domain.TagScopeRef
import io.medatarun.tags.core.ports.needs.TagScopeRegistry

class TagScopesImpl(val tagScopeRegistry: TagScopeRegistry) : TagScopes {
    override fun ensureLocalScopeExists(ref: TagScopeRef) {
        val localScopeRef = ref as? TagScopeRef.Local
            ?: throw TagFreeCommandIncompatibleTagScopeRefException(ref.asString())
        val scopeManager = tagScopeRegistry.findManagerByScopeType(localScopeRef.type)
            ?: throw TagScopeManagerNotFoundException(localScopeRef.type.value)
        val exists = scopeManager.localScopeExists(localScopeRef)
        if (!exists) throw TagScopeNotFoundException(localScopeRef.asString())
    }
}