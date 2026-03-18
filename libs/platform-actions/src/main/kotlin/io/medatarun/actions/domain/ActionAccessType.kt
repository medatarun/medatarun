package io.medatarun.actions.domain

/**
 * Indicates the strategy name used to invoke the action (maybe expanded in the future, had contained other values before we keep it)
 */
enum class ActionAccessType {
    /** Create an event and send it using the dispatch method */
    DISPATCH
}