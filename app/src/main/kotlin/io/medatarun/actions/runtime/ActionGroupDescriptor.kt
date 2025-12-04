package io.medatarun.actions.runtime

import io.medatarun.actions.providers.ActionProviders
import kotlin.reflect.KProperty1

data class ActionGroupDescriptor(
    /**
     * Group identifier (the name of the property on the providers class)
     */
    val name: String,
    /**
     * Property that gives access to this group in provider
     */
    val property: KProperty1<ActionProviders, *>,
    /**
     * List of commands provided by this group
     */
    val commands: List<ActionCmdDescriptor>,
)