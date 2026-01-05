package io.medatarun.actions.runtime

import io.medatarun.actions.ports.needs.ActionProvider

data class ActionGroupDescriptor(
    /**
     * Group identifier (the name of the property on the providers class)
     */
    val key: String,
    /**
     * List of commands provided by this group
     */
    val actions: List<ActionCmdDescriptor>,
    /**
     * Provider that provides commands for this group
     */
    val providerInstance: ActionProvider<*>,
)