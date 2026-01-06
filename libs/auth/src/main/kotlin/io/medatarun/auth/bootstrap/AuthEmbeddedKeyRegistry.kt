package io.medatarun.auth.bootstrap

/**
 * Manages keys in a keystore.
 *
 * In Medatarun, keys are stored in MEDATARUN_HOME/secrets/auth/issuer
 *
 * There are 3 files:
 * - issuer.key.pem: private signing key
 * - issuer.pub.pem: public key
 * - issuer.kid: contains the key unique identifier
 */
interface AuthEmbeddedKeyRegistry {

    fun loadOrCreateKeys(): JwtKeyMaterial

    companion object {
        /**
         * Default location for keys relative to MEDATARUN_HOME
         */
        const val defaultKeystorePath: String = "secrets/auth/issuer"
    }
}