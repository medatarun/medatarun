package io.medatarun.actions.runtime

/**
 * Documentation of a command
 */
@Target(AnnotationTarget.CLASS)
annotation class ActionDoc(val title: String, val description: String = "")