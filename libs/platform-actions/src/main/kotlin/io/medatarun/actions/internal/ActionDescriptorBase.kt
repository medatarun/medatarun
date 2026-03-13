package io.medatarun.actions.internal

import io.medatarun.actions.domain.ActionAccessType
import io.medatarun.actions.domain.ActionId
import kotlin.reflect.KType

internal class ActionDescriptorBase(
    /**
     * Unique identifier of action across all actions of all extensions.
     * Identifier is mostly internal to the system and not meant to be used by
     * end users.
     */
    val id: ActionId,
    /**
     * Serializable unique name of the action in its group
     */
    val key: String,
    /**
     * Name of the command
     */
    val actionClassName: String,
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
     * How to invoke the command (may be expanded in the future, had contained other values before, we keep it)
     */
    val accessType: ActionAccessType,
    /**
     * Hint for UI to know where to display the command
     * Known values are "" (no location, hidden), "models" in the list of models, "entity" on an entity, etc.
     * This is tight to the UI. To know possible values, you need to look at the UI pages' code
     */
    val uiLocations: Set<String>,
    /**
     * Name of security rule tied to this action
     */
    val securityRule: String,

    )