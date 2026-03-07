package io.medatarun.actions.domain

import io.medatarun.actions.ports.needs.ActionDocSemanticsIntent
import io.medatarun.actions.ports.needs.ActionDocSemanticsMode

/**
 * Represent how action semantics are configured on an action, typically with [io.medatarun.actions.ports.needs.ActionDoc] annotation or other sources of actions.
 */
sealed class ActionSemanticsConfig(
    /**
     * [mode] tells how declaration is done (no semantics, automatic mode, unknown or real declaration)
     */
    val mode: ActionDocSemanticsMode,
) {
    /**
     * [None] means that this action has no semantics at all
     */
    object None : ActionSemanticsConfig(ActionDocSemanticsMode.NONE)

    /**
     * [Auto] means that this action has semantics but should be auto discovered by
     * the semantics inferrer.
     */
    object Auto : ActionSemanticsConfig(ActionDocSemanticsMode.AUTO)

    /**
     * [Unknown] should be considered as an error, no action should return unknown.
     * We will remove it later once this is finished.
     */
    object Unknown : ActionSemanticsConfig(ActionDocSemanticsMode.UNKNOWN)

    /**
     * [Declared] means the action descriptor carries a fully described in action descriptor.
     * There will be no attempts of auto discovery and information will be taken as-is.
     */
    data class Declared(
        /**
         * Name of the intent in our intent vocabulary
         */
        val intent: ActionDocSemanticsIntent,
        /**
         * List of parameters of this action impacted. Format of a subject is
         *
         * ```type(param,param,param)```
         *
         * for example
         *
         * ```model(modelRef)``` or ```entity(modelRef,entityId)```
         *
         * `model` and `entity` are subject types
         *
         * `modelRef`, `entityId` are action parameters keys that are used to identity impacted subjects.
         *
         */
        val subjects: List<String>
    ) : ActionSemanticsConfig(ActionDocSemanticsMode.DECLARED)
}
