package io.medatarun.resources

/**
 * Documentation of a command
 */
@Target(AnnotationTarget.CLASS)
annotation class ResourceCommandDoc(val title: String, val description: String = "")
