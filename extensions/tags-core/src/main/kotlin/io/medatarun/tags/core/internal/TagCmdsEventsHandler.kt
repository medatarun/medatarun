package io.medatarun.tags.core.internal

import io.medatarun.platform.kernel.Event
import io.medatarun.platform.kernel.EventNotifier
import io.medatarun.tags.core.domain.TagId
import io.medatarun.tags.core.ports.needs.TagCmdsEvents

/**
 * Forwards tag lifecycle events to all registered scope managers.
 * Managers may throw to veto an operation according to their local rules.
 */
class TagCmdsEventsHandler(
    private val createNotifier: EventNotifier<TagBeforeDeleteEvt>
) : TagCmdsEvents {
    override fun onBeforeDelete(tagId: TagId) {
        createNotifier.fire(TagBeforeDeleteEvt(tagId))
    }
}

data class TagBeforeDeleteEvt(val id: TagId): Event