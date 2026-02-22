package io.medatarun.tags.core.ports.needs

import io.medatarun.tags.core.domain.TagId

/**
 * Receives tag lifecycle events emitted by TagCmds.
 * Implementations may throw exceptions to veto the underlying command.
 */
interface TagEventListener {
    fun onBeforeDelete(tagId: TagId)
}
