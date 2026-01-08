package io.medatarun.auth.ports.exposed

import io.medatarun.auth.domain.Jwks
import io.medatarun.auth.domain.User
import java.security.interfaces.RSAPublicKey

interface AuthEmbeddedService {
    fun oidcJwks(): Jwks
    fun oidcJwksUri(): String
    fun oidcPublicKey(): RSAPublicKey
    fun oidcIssuer(): String
    fun oidcAudience(): String
    fun oidcLogin(login: String, password: String): JwtTokenResponse

    fun loadOrCreateBootstrapSecret(runOnce: (secret: String) -> Unit)
    fun adminBootstrap(secret: String, login: String, fullname: String, password: String): JwtTokenResponse
    fun createEmbeddedUser(login: String, fullname: String, clearPassword: String, admin: Boolean): User
    fun changeOwnPassword(sub: String, oldPassword: String, newPassword: String)
    fun changeUserPassword(login: String, newPassword: String)
}