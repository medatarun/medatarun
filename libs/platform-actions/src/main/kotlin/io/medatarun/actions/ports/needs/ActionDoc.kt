package io.medatarun.actions.ports.needs

/**
 * Documentation of a command
 */
@Target(AnnotationTarget.CLASS)
annotation class ActionDoc(
    val key: String,
    val title: String,
    val description: String = "",
    val uiLocations: Array<String>,
    val securityRule: String
)