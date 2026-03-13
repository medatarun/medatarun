package io.medatarun.actions.domain

import io.medatarun.actions.ports.needs.ActionDocSemanticsIntent

/**
 * Define the semantics resolved for an action, meaning
 * "how in structured data we define this action so that other
 * tools can understand what the action do and react accordingly".
 */
interface ActionSemantics {
    /**
     * Action intent (create something, delete something, update something, read, unknown etc.)
     */
    val intent: ActionDocSemanticsIntent

    /**
     * What subjects (business concepts) are concerned by this action when the action modifies or changes something.
     * This helps callers navigate or manage caches.
     */
    val subjects: List<ActionSemanticsSubject>

    /**
     * What are the kind of business concepts are concerned by this action in terms of returned results.
     * This helps maintain caches on callers.
     */
    val returns: List<String>
}

/**
 * Subject of an action (when action modifies something)
 */
interface ActionSemanticsSubject {
    /**
     * Type is the name of the business concept that holds the subject (in medatarun can be actor, model, prompt, entity, etc.)
     */
    val type: String

    /**
     * Tells what parameters, in action payload, point to the subject reference (for example modelId, modelRef, promptId, etc.).
     *
     * A subject may be referenced by many parameters, for example, entity is referenced by modelId + entityId
     */
    val referencingParams: List<ActionSemanticsSubjectReferencingParam>
}

/**
 * Tells what parameters, in action payload, point to the subject reference (for example modelId, modelRef, promptId, etc.)
 */
data class ActionSemanticsSubjectReferencingParam(
    /**
     * Parameter name
     */
    val name: String,
    /**
     * Kind of assignment (id, ref, or something else). Tells how to interpret the parameter and how to resolve subject identity.
     */
    val kind: ActionSemanticsSubjectReferencingParamKind
)

enum class ActionSemanticsSubjectReferencingParamKind {
    /** When a parameter is an Id */
    ID,
    /** When a parameter is a Ref (modelRef, entityRef, etc.) */
    REF,
    /** When a parameter is a Key (key, modelKey, entityKey, promptKey, etc.) */
    KEY
}
