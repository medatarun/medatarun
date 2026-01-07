package io.medatarun.auth.embedded.internal

import io.medatarun.auth.embedded.*
import java.nio.file.Path
import java.security.interfaces.RSAPublicKey
import java.util.*

class AuthEmbeddedServiceImpl(
    private val bootstrapDirPath: Path,
    private val keyStorePath: Path,
    private val userStorage: UserStore
) : AuthEmbeddedService {
    private val authEmbeddedKeyRegistry = AuthEmbeddedKeyRegistryImpl(keyStorePath)
    private val authEmbeddedKeys = authEmbeddedKeyRegistry.loadOrCreateKeys()
    private val bootstrapper = AuthEmbeddedBootstrapSecretImpl(bootstrapDirPath)
    private val authEmbeddedPwd = AuthEmbeddedPwd()

    private val jwtCfg = AuthEmbeddedJwtConfig(
        issuer = "urn:medatarun:${authEmbeddedKeys.kid}",  // stable tant que tes fichiers sont là
        audience = "medatarun",
        ttlSeconds = 3600
    )
    private val tokenIssuer = AuthEmbeddedJwtTokenIssuerImpl(authEmbeddedKeys, jwtCfg)

    override fun oidcPublicKey(): RSAPublicKey {
        return authEmbeddedKeys.publicKey
    }

    override fun oidcIssuer(): String {
        return jwtCfg.issuer
    }

    override fun oidcAudience(): String {
        return jwtCfg.audience
    }

    override fun oidcJwksUri(): String {
        return "/jwks.json"
    }

    override fun oidcJwks(): Jwks {
        return JwksAdapter.toJwks(authEmbeddedKeys.publicKey, authEmbeddedKeys.kid)
    }

    override fun loadOrCreateBootstrapSecret(runOnce: (secret: String) -> Unit) {
        bootstrapper.loadOrCreateBootstrapSecret(runOnce)
    }

    override fun adminBootstrap(secret: String, login: String, fullName: String, password: String): JwtTokenResponse {
        val bootstrapState = bootstrapper.load() ?: throw AuthEmbeddedServiceBootstrapNotReadyException()
        if (bootstrapState.consumed) throw AuthEmbeddedBoostrapAlreadyConsumedException()
        if (bootstrapState.secret != secret) throw AuthEmbeddedBoostrapBadSecretException()
        val token = tokenIssuer.issueToken(
            sub = "admin",
            claims = mapOf("role" to "admin", "bootstrap" to true)
        )
        createEmbeddedUserInternal(
            login = login,
            fullName = fullName,
            clearPassword = password,
            admin = true,
            bootstrap = true
        )
        bootstrapper.markBootstrapConsumed()
        return JwtTokenResponse(token, "Bearer", jwtCfg.ttlSeconds)
    }

    override fun createEmbeddedUser(login: String, fullName: String, clearPassword: String, admin: Boolean) {
        return createEmbeddedUserInternal(login, fullName, clearPassword, admin, false)
    }

    fun createEmbeddedUserInternal(
        login: String,
        fullName: String,
        clearPassword: String,
        admin: Boolean,
        bootstrap: Boolean
    ) {
        val checkPasswordPolicy = authEmbeddedPwd.checkPasswordPolicy(clearPassword, login)
        if (checkPasswordPolicy is AuthEmbeddedPwd.PasswordCheck.Fail)
            throw AuthEmbeddedCreateUserPasswordFailException(checkPasswordPolicy.reason)
        val password = authEmbeddedPwd.hashPassword(clearPassword)
        userStorage.insert(UUID.randomUUID().toString(), login, fullName, password, admin, bootstrap, null)
    }
}