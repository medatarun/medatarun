package io.medatarun.actions.runtime

import kotlin.reflect.KType

data class ActionCmdDescriptor(
    /**
     * Name of the command
     */
    val name: String,
    /**
     * Name (identifier) of the group that provides the command
     */
    val group: String,
    /**
     * Human title of the command
     */
    val title: String?,
    /**
     * Human description of the command
     */
    val description: String?,
    /**
     * Expected result type
     */
    val resultType: KType,
    /**
     * List of the command parameters
     */
    val parameters: List<ActionCmdParamDescriptor>,
    /**
     * How to invoke the command (may be expanded in the future, had contained other values before, we keep it)
     */
    val accessType: ActionCmdAccessType
)