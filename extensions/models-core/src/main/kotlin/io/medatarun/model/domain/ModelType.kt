package io.medatarun.model.domain


/**
 * Defines one of the types known by the model, that can be used as [AttributeDef]
 */
interface ModelType {
    /**
     * Unique identifier in the application instance and more generally across all instances since it is backed by UUID
     */
    val id: TypeId

    /**
     * Unique type key in the model, unique in the model
     */
    val key: TypeKey

    /**
     * Display name of the type
     */
    val name: LocalizedText?

    /**
     * Display description of the type
     */
    val description: LocalizedMarkdown?
}