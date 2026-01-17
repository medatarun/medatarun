package io.medatarun.auth.ports.exposed

import io.medatarun.auth.domain.jwt.JwtKeyMaterial

/**
 * Manages the bootstrap key in the keystore for embeeded auth server.
 *
 * In Medatarun, bootstrap key is stored in MEDATARUN_HOME/secrets/auth/issuer
 *
 * There are 3 files:
 * - issuer.key.pem: private signing key
 * - issuer.pub.pem: public key
 * - issuer.kid: contains the key unique identifier
 */
interface JwtInternalSigninKeyRegistry {

    fun loadOrCreateKeys(): JwtKeyMaterial



    companion object {
        /**
         * Default location for keys relative to MEDATARUN_HOME
         */
        const val DEFAULT_KEYSTORE_PATH_NAME: String = "data/secrets/auth/issuer"

    }
}