package io.medatarun.auth.bootstrap.internal

/**
 * Represents the bootstrap secret
 */
data class AuthEmbeddedBootstrapState(
    /**
     * Secret phrase
     */
    val secret: String,
    /**
     * Indicates if already consumed or not
     */
    val consumed: Boolean
)