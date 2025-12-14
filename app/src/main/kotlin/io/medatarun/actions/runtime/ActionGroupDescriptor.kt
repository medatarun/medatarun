package io.medatarun.actions.runtime

import io.medatarun.actions.ports.needs.ActionProvider

data class ActionGroupDescriptor(
    /**
     * Group identifier (the name of the property on the providers class)
     */
    val name: String,
    /**
     * List of commands provided by this group
     */
    val commands: List<ActionCmdDescriptor>,
    /**
     * Provider that provides commands for this group
     */
    val providerInstance: ActionProvider<*>,
)