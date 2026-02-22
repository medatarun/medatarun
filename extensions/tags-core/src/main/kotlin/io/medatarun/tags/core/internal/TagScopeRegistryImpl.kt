package io.medatarun.tags.core.internal

import io.medatarun.tags.core.domain.TagScopeType
import io.medatarun.tags.core.domain.TagDuplicateScopeManagerException
import io.medatarun.tags.core.ports.needs.TagScopeManager
import io.medatarun.tags.core.ports.needs.TagScopeManagerResolver
import io.medatarun.tags.core.ports.needs.TagScopeRegistry

class TagScopeRegistryImpl(
    private val resolver: TagScopeManagerResolver
) : TagScopeRegistry {

    private fun resolveItems(): List<TagScopeManager> {
        return resolver.findScopeManagers()
    }

    private fun resolveByType(): Map<String, TagScopeManager> {
        val byType = mutableMapOf<String, TagScopeManager>()
        resolveItems().forEach { manager ->
            val previous = byType.put(manager.type.value, manager)
            if (previous != null) {
                throw TagDuplicateScopeManagerException(manager.type.value)
            }
        }
        return byType
    }

    override fun findManagerByScopeType(type: TagScopeType): TagScopeManager? {
        return resolveByType()[type.value]
    }

    override fun findAllManagers(): List<TagScopeManager> {
        return resolveItems()
    }
}
