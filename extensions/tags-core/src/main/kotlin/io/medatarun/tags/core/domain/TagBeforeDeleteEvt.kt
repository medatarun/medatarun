package io.medatarun.tags.core.domain

import io.medatarun.platform.kernel.Event
import io.medatarun.security.AppTraceabilityRecord

/**
 * Event emitted by `tags-core` before deleting one explicit tag.
 *
 * Purpose:
 * - give other modules a chance to remove references to this tag id from their own objects
 * - allow listeners to veto the deletion by throwing before storage mutation happens
 *
 * Emission contract:
 * - emitted for explicit tag deletions handled as tag operations (`TagFreeDelete`, `TagManagedDelete`)
 * - emitted for managed-tag deletions caused by `TagGroupDelete` (one event per managed tag)
 * - not emitted for bulk scope cleanup triggered by `TagScopeBeforeDeleteEvent` / `TagCmd.TagScopeDelete`
 *
 * Consumer contract:
 * - listeners should only handle cleanup/veto logic related to the provided tag id
 * - listeners must not interpret this event as a scope-level deletion signal
 *
 * This event models a per-tag deletion lifecycle hook, not a generic notification for every path that may remove tags.
 */
data class TagBeforeDeleteEvt(
    val id: TagId,
    val traceabilityRecord: AppTraceabilityRecord
) : Event
