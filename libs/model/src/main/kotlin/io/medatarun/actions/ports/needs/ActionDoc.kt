package io.medatarun.actions.ports.needs

/**
 * Documentation of a command
 */
@Target(AnnotationTarget.CLASS)
annotation class ActionDoc(
    val title: String,
    val description: String = "",
    val uiLocation: String
)