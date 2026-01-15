package io.medatarun.auth.internal.bootstrap

/**
 * Represents the bootstrap secret
 */
data class BootstrapSecretState(
    /**
     * Secret phrase
     */
    val secret: String,
    /**
     * Indicates if already consumed or not
     */
    val consumed: Boolean
)