package io.medatarun.actions.domain

import io.medatarun.actions.ports.needs.ActionProvider

data class ActionGroupDescriptor(
    /**
     * Group identifier (the name of the property on the providers class)
     */
    val key: String,
    /**
     * Provider that provides commands for this group
     */
    val providerInstance: ActionProvider<*>,
)