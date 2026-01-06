package io.medatarun.auth.embedded.internal

import io.medatarun.auth.embedded.AuthEmbeddedJwtConfig
import io.medatarun.auth.embedded.AuthEmbeddedService
import io.medatarun.auth.embedded.Jwks
import java.nio.file.Path

class AuthEmbeddedServiceImpl(
    private val bootstrapDirPath: Path,
    private val keyStorePath: Path
): AuthEmbeddedService {
    private val authEmbeddedKeyRegistry = AuthEmbeddedKeyRegistryImpl(keyStorePath)
    private val authEmbeddedKeys = authEmbeddedKeyRegistry.loadOrCreateKeys()
    private val authEmbeddedBootstrapSecret = AuthEmbeddedBootstrapSecretImpl(bootstrapDirPath)
    private val jwtCfg = AuthEmbeddedJwtConfig(
        issuer = "urn:medatarun:${authEmbeddedKeys.kid}",  // stable tant que tes fichiers sont là
        audience = "medatarun",
        ttlSeconds = 3600
    )

    override fun oidcJwksUri(): String {
        return "/jwks.json"
    }
    override fun oidcJwks(): Jwks {
        return JwksAdapter.toJwks(authEmbeddedKeys.publicKey, authEmbeddedKeys.kid)
    }

    override fun loadOrCreateBootstrapSecret(runOnce: (secret: String) -> Unit) {
        authEmbeddedBootstrapSecret.loadOrCreateBootstrapSecret(runOnce)
    }

    override fun adminBootstrap(secret: String, username: String, password: String) {
        TODO("Not yet implemented")
    }
}