package io.medatarun.auth.ports.exposed

import io.medatarun.auth.internal.bootstrap.BootstrapSecretState

/**
 * Manages the first bootstrap secret, that can only be used once
 */
interface BootstrapSecretLifecycle {

    /**
     * Loads or create the bootstrap secret.
     *
     * Once the bootstrap secret had been created, calls back [logOnce] with the secret,
     * typically to display it in logs the first time, so that an admin can know him.
     */
    fun loadOrCreateBootstrapSecret(logOnce: (String) -> Unit): BootstrapSecretState

    /**
     * Marks the bootstrap secret consumed
     */
    fun markBootstrapConsumed()

    /**
     * Loads the boostrap secret, consumed or not, but do not generate it
     */
    fun load(): BootstrapSecretState?

    companion object {
        const val DEFAULT_BOOTSTRAP_SECRET_PATH_NAME = "data/secrets/auth/bootstrap"
        const val SECRET_MIN_SIZE = 20
        const val SECRET_SIZE = 48
    }
}