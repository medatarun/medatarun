package io.medatarun.tags.core.internal

import io.medatarun.tags.core.domain.TagId
import io.medatarun.tags.core.ports.needs.TagCmdsEvents
import io.medatarun.tags.core.ports.needs.TagScopeRegistry

/**
 * Forwards tag lifecycle events to all registered scope managers.
 * Managers may throw to veto an operation according to their local rules.
 */
class TagCmdsEventsHandler(
    private val tagScopeRegistry: TagScopeRegistry
) : TagCmdsEvents {
    override fun onBeforeDelete(tagId: TagId) {
        tagScopeRegistry.findAllManagers().forEach { manager ->
            manager.onBeforeTagDelete(tagId)
        }
    }
}
