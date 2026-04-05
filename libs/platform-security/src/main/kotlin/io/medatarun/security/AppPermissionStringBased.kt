package io.medatarun.security

/**
 * Creates a permission from a String.
 * Model should prefer creating their own class.
 * This is mostly to help with tests and prototyping.
 */
data class AppPermissionStringBased(override val key: String): AppPermission