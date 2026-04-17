package io.medatarun.actions.domain

import kotlin.reflect.KType

/**
 * Declaration of an action (by an external module). This is the public facing declaration.
 */
interface ActionDescriptor {
    /**
     * Unique identifier of action across all actions of all extensions.
     * Identifier is mostly internal to the system and not meant to be used by
     * end users.
     */
    val id: ActionId
    /**
     * Serializable unique name of the action in its group
     */
    val key: String
    /**
     * Name of the command
     */
    val actionClassName: String
    /**
     * Name (identifier) of the group that provides the command
     */
    val group: String
    /**
     * Human title of the command
     */
    val title: String?
    /**
     * Human description of the command
     */
    val description: String?
    /**
     * Expected result type
     */
    val resultType: KType
    /**
     * List of the command parameters
     */
    val parameters: List<ActionDescriptorParam>
    /**
     * How to invoke the command (maybe expanded in the future, had contained other values before, we keep it)
     */
    val accessType: ActionAccessType
    /**
     * Name of security rule tied to this action
     */
    val securityRule: String
    /**
     * Semantics of action as declared where action is declared.
     */
    val semantics: ActionSemanticsConfig


    fun findParamByName(name: String): ActionDescriptorParam? {
        return parameters.firstOrNull { it.key == name }
    }
}