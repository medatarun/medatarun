package io.medatarun.auth.embedded

interface AuthEmbeddedService {
    fun oidcJwks(): Jwks
    fun oidcJwksUri(): String
    fun loadOrCreateBootstrapSecret(runOnce: (secret: String) -> Unit)
    fun adminBootstrap(secret: String, username: String, password: String)
}