package io.medatarun.model.domain

import io.medatarun.type.commons.text.TextMarkdown
import io.medatarun.type.commons.text.TextSingleLine


/**
 * Defines one of the types known by the model, that can be used as [Attribute]
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
    val name: TextSingleLine?

    /**
     * Display description of the type
     */
    val description: TextMarkdown?
}