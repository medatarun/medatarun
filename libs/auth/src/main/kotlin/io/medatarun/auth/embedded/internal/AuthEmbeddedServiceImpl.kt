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

        val user = createEmbeddedUserInternal(
            id=UUID.randomUUID(),
            login = login,
            fullName = fullName,
            clearPassword = password,
            admin = true,
            bootstrap = true
        )

        bootstrapper.markBootstrapConsumed()
        return createTokenForUser(user)
    }

    override fun createEmbeddedUser(login: String, fullName: String, clearPassword: String, admin: Boolean): User {
        return createEmbeddedUserInternal(UUID.randomUUID(), login, fullName, clearPassword, admin, false)
    }

    override fun oidcLogin(login: String, password: String): JwtTokenResponse {
        val user = userStorage.findByLogin(login) ?: throw AuthEmbeddedBadCredentialsException()
        if (user.disabledDate != null) throw AuthEmbeddedBadCredentialsException()
        val valid = authEmbeddedPwd.verifyPassword(user.passwordHash, password)
        if (!valid) throw AuthEmbeddedBadCredentialsException()
        return createTokenForUser(user)
    }

    fun createTokenForUser(user: User): JwtTokenResponse {
        val token = tokenIssuer.issueToken(
            sub = user.login,
            claims = mapOf(
                "name" to user.fullName,
                "role" to (if (user.admin) "admin" else ""),
                "bootstrap" to true,
                "mid" to user.id.toString()
            )
        )
        return JwtTokenResponse(token, "Bearer", jwtCfg.ttlSeconds)
    }

    fun createEmbeddedUserInternal(
        id: UUID,
        login: String,
        fullName: String,
        clearPassword: String,
        admin: Boolean,
        bootstrap: Boolean
    ): User {
        val checkPasswordPolicy = authEmbeddedPwd.checkPasswordPolicy(clearPassword, login)
        if (checkPasswordPolicy is AuthEmbeddedPwd.PasswordCheck.Fail)
            throw AuthEmbeddedCreateUserPasswordFailException(checkPasswordPolicy.reason)
        val password = authEmbeddedPwd.hashPassword(clearPassword)

        userStorage.insert(id.toString(), login, fullName, password, admin, bootstrap, null)
        return User(
            id = id,
            login = login,
            fullName = fullName,
            passwordHash = password,
            admin = admin,
            bootstrap = bootstrap,
            disabledDate = null
        )
    }
}