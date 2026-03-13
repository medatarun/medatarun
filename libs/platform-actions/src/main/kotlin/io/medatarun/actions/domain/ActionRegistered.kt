package io.medatarun.actions.domain

/**
 * Action registered in the action registry
 */
interface ActionRegistered {
    /**
     * Action descriptor
     */
    val descriptor: ActionDescriptor

    /**
     * Resolved action semantics (not to be confused with [descriptor]'s declared semantics)
     */
    val semantics: ActionSemantics
}