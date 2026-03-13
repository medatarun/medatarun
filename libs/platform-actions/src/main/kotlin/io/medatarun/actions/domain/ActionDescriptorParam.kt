package io.medatarun.actions.domain

import io.medatarun.types.TypeJsonEquiv
import kotlin.reflect.KType

/**
 * Declaration of a parameter of an action.
 *
 * This is the public declaration of an action, as provided by external modules.
 */
interface ActionDescriptorParam {
    /**
     * Parameter key, unique in action. Serialized name of the parameter as used in JSON.
     */
    val key: String

    /**
     * Human-readable title of the parameter
     */
    val title: String?

    /**
     * Java/Kotlin type of the parameter. We use KType before it can hold compile time information such as generics.
     */
    val type: KType

    /**
     * When using Medatarun features in other languages, this is the common exact type name used in other language mappings.
     */
    val multiplatformType: String

    /**
     * JSON type we must use for Json representation.
     */
    val jsonType: TypeJsonEquiv

    /**
     * Indicates the parameter is optional (understand nullable)
     */
    val optional: Boolean

    /**
     * When displaying the list of parameters of contained action, parameters are order by [order] ascending
     */
    val order: Int

    /**
     * Human-readable description of the parameter (usage, constraints, etc.) Serves as documentation.
     */
    val description: String?
}

