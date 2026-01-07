package io.medatarun.auth.embedded

import io.medatarun.auth.embedded.internal.AuthEmbeddedBootstrapState

/**
 * Manages the first bootstrap secret, that can only be used once
 */
interface AuthEmbeddedBootstrapSecret {

    /**
     * Loads or create the bootstrap secret.
     *
     * Once the bootstrap secret had been created, calls back [logOnce] with the secret,
     * typically to display it in logs the first time, so that an admin can know him.
     */
    fun loadOrCreateBootstrapSecret(logOnce: (String) -> Unit): AuthEmbeddedBootstrapState

    /**
     * Marks the bootstrap secret consumed
     */
    fun markBootstrapConsumed()
    fun load(): AuthEmbeddedBootstrapState?

    companion object {
        const val DEFAULT_BOOTSTRAP_SECRET_PATH_NAME = "secrets/auth/bootstrap"
    }
}
