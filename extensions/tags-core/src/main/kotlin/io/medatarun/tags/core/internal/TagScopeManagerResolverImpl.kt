package io.medatarun.tags.core.internal

import io.medatarun.platform.kernel.ExtensionRegistry
import io.medatarun.tags.core.ports.needs.TagScopeManager
import io.medatarun.tags.core.ports.needs.TagScopeManagerResolver

class TagScopeManagerResolverImpl(
    private val extensionRegistry: ExtensionRegistry
) : TagScopeManagerResolver {
    override fun findScopeManagers(): List<TagScopeManager> {
        return extensionRegistry.findContributionsFlat(TagScopeManager::class)
    }
}
