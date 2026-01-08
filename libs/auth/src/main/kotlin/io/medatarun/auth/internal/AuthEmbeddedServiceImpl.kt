package io.medatarun.auth.internal

import io.medatarun.auth.domain.*
import io.medatarun.auth.ports.exposed.AuthEmbeddedService
import io.medatarun.auth.ports.exposed.JwtTokenResponse
import io.medatarun.auth.ports.needs.AuthClock
import io.medatarun.auth.ports.needs.UserStore
import java.nio.file.Path
import java.security.interfaces.RSAPublicKey
import java.util.*

class AuthEmbeddedServiceImpl(
    private val bootstrapDirPath: Path,
    private val keyStorePath: Path,
    private val userStorage: UserStore,
    private val clock: AuthClock,
    private val passwordEncryptionIterations: Int
) : AuthEmbeddedService {
    private val authEmbeddedKeyRegistry = AuthEmbeddedKeyRegistryImpl(keyStorePath)
    private val authEmbeddedKeys = authEmbeddedKeyRegistry.loadOrCreateKeys()
    private val bootstrapper = AuthEmbeddedBootstrapSecretImpl(bootstrapDirPath)
    private val authEmbeddedPwd = AuthEmbeddedPwd(passwordEncryptionIterations)

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

    override fun adminBootstrap(secret: String, login: String, fullname: String, password: String): JwtTokenResponse {
        val bootstrapState = bootstrapper.load() ?: throw AuthEmbeddedServiceBootstrapNotReadyException()
        if (bootstrapState.consumed) throw AuthEmbeddedBootstrapAlreadyConsumedException()
        if (bootstrapState.secret != secret) throw AuthEmbeddedBootstrapBadSecretException()

        val user = createEmbeddedUserInternal(
            id=UUID.randomUUID(),
            login = login,
            fullname = fullname,
            clearPassword = password,
            admin = true,
            bootstrap = true
        )

        bootstrapper.markBootstrapConsumed()
        return createTokenForUser(user)
    }

    override fun createEmbeddedUser(login: String, fullname: String, clearPassword: String, admin: Boolean): User {
        return createEmbeddedUserInternal(UUID.randomUUID(), login, fullname, clearPassword, admin, false)
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
                "name" to user.fullname,
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
        fullname: String,
        clearPassword: String,
        admin: Boolean,
        bootstrap: Boolean
    ): User {
        val checkPasswordPolicy = authEmbeddedPwd.checkPasswordPolicy(clearPassword, login)
        if (checkPasswordPolicy is AuthEmbeddedPwd.PasswordCheck.Fail)
            throw AuthEmbeddedCreateUserPasswordFailException(checkPasswordPolicy.reason)
        val password = authEmbeddedPwd.hashPassword(clearPassword)

        val found = userStorage.findByLogin(login)
        if (found != null) throw AuthEmbeddedUserAlreadyExistsException()

        userStorage.insert(id.toString(), login, fullname, password, admin, bootstrap, null)
        return User(
            id = id,
            login = login,
            fullname = fullname,
            passwordHash = password,
            admin = admin,
            bootstrap = bootstrap,
            disabledDate = null
        )
    }

    override fun changeOwnPassword(username: String, oldPassword: String, newPassword: String) {
        val user = userStorage.findByLogin(username) ?: throw AuthEmbeddedBadCredentialsException()
        val valid = authEmbeddedPwd.verifyPassword(user.passwordHash, oldPassword)
        if (!valid) throw AuthEmbeddedBadCredentialsException()
        val policyCheck = authEmbeddedPwd.checkPasswordPolicy(newPassword, username)
        if (policyCheck is AuthEmbeddedPwd.PasswordCheck.Fail)
            throw AuthEmbeddedCreateUserPasswordFailException(policyCheck.reason)
        val newPasswordHash = authEmbeddedPwd.hashPassword(newPassword)
        userStorage.updatePassword(username, newPasswordHash)
    }

    override fun changeUserPassword(login: String, newPassword: String) {
        val user = userStorage.findByLogin(login) ?: throw AuthEmbeddedUserNotFoundException()
        val policyCheck = authEmbeddedPwd.checkPasswordPolicy(newPassword, login)
        if (policyCheck is AuthEmbeddedPwd.PasswordCheck.Fail)
            throw AuthEmbeddedCreateUserPasswordFailException(policyCheck.reason)
        val newPasswordHash = authEmbeddedPwd.hashPassword(newPassword)
        userStorage.updatePassword(user.login, newPasswordHash)
    }

    override fun disableUser(username: String) {
        userStorage.disable(username, at = clock.now())
    }
}