package io.medatarun.auth.domain

/**
 * Password in clear form (not hashed)
 */
@JvmInline
value class PasswordClear(val value: String)