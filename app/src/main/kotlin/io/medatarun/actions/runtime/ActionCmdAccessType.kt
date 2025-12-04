package io.medatarun.actions.runtime

/**
 * Indicates how to invoke the command (may be expanded in the future, had contained other values before, we keep it)
 */
enum class ActionCmdAccessType {
    /** Create an event and send it using the dispatch method */
    DISPATCH
}