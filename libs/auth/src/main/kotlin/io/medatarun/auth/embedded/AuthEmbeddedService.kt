package io.medatarun.auth.embedded

import java.security.interfaces.RSAPublicKey

interface AuthEmbeddedService {
    fun oidcJwks(): Jwks
    fun oidcJwksUri(): String
    fun oidcPublicKey(): RSAPublicKey
    fun oidcIssuer(): String
    fun oidcAudience(): String
    fun oidcLogin(login: String, password: String): JwtTokenResponse

    fun loadOrCreateBootstrapSecret(runOnce: (secret: String) -> Unit)
    fun adminBootstrap(secret: String, login: String, fullName: String, password: String): JwtTokenResponse
    fun createEmbeddedUser(login: String, fullName: String, clearPassword: String, admin: Boolean): User
}