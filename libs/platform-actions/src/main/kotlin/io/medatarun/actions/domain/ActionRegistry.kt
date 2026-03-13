package io.medatarun.actions.domain

/**
 * Public interface for Action registry. Allow other modules to search in registry
 */
interface ActionRegistry {
    fun findAction(actionGroupKey: String, actionKey: String): ActionRegistered
    fun findAllActions(): Collection<ActionRegistered>
}