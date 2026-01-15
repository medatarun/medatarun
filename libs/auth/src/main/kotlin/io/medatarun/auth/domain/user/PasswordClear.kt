package io.medatarun.auth.domain.user

/**
 * Password in clear form (not hashed)
 */
@JvmInline
value class PasswordClear(val value: String)