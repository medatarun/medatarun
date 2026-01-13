package io.medatarun.model.domain


/**
 * Defines one of the types known by the model, that can be used as [AttributeDef]
 */
interface ModelType {
    /**
     * Unique type identifier in the model
     */
    val id: TypeKey

    /**
     * Display name of the type
     */
    val name: LocalizedText?
    /**
     * Display description of the type
     */
    val description: LocalizedText?
}