package io.medatarun.lang.io.medatarun.lang.config

/**
 * Simple interface to mark accross the code all declaration
 * of configuration properties and standardize documentation efforts.
 */
interface ConfigPropertyDescription {
    /** Key of the property, in property format (canonical name) */
    val key: String
    /** Type hint */
    val type: String
    /** Default value hint */
    val defaultValue:String
    /** Full description */
    val description: String
}