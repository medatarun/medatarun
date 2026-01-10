package io.medatarun.lang.io.medatarun.lang.config

/**
 * Simple interface to mark accross the code all declaration
 * of configuration properties and standardize documentation efforts.
 */
interface ConfigPropertyDescription {
    val key: String
    val type: String
    val defaultValue:String
    val description: String
}