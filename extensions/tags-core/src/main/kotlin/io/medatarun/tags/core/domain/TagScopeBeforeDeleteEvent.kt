package io.medatarun.tags.core.domain

import io.medatarun.platform.kernel.Event

/**
 * Event emitted by the owner of a local scope before deleting that scope root.
 *
 * Purpose:
 * - notify `tags-core` that all tags defined in this local scope must be deleted
 *
 * Emission contract:
 * - emitted by the module that owns the scope (for example, a model/prompt/recipe/vehicle module)
 * - emitted before the scope root is deleted
 * - expresses the owner's intent to delete the whole scope and the objects it contains
 *
 * Consumer contract:
 * - `tags-core` consumes this event and performs a scope-level tag cleanup (`TagCmd.TagScopeDelete`)
 * - consumers must not assume `TagBeforeDeleteEvt` will be emitted for each tag deleted through this scope event
 *
 * Responsibility split:
 * - scope owner module remains responsible for deleting/cleaning its own objects and local data
 * - `tags-core` is responsible for deleting tag definitions that belong to the provided scope
 *
 * This event is a bulk scope-deletion signal. It is not equivalent to a sequence of explicit per-tag deletions.
 */
data class TagScopeBeforeDeleteEvent(val tagScopeRef: TagScopeRef): Event