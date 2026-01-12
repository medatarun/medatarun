package io.medatarun.auth.domain

import java.time.Instant

/**
 * User in our embedded identity provider
 */
data class User(
    /**
     * Unique user identifier
     */
    val id: UserId,
    /**
     * User name (login)
     */
    val username: Username,
    /**
     * Full name for display
     */
    val fullname: Fullname,
    /**
     * Password hash (be careful to never log that)
     */
    val passwordHash: PasswordHash,
    /**
     * Tells if user is admin
     */
    val admin: Boolean,
    /**
     * Tells if the user had been created by bootstrap process
     */
    val bootstrap: Boolean,
    /**
     * Date user had been disabled
     */
    val disabledDate: Instant?
)