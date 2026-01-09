package io.medatarun.auth.internal

import io.medatarun.auth.domain.*
import io.medatarun.auth.ports.exposed.AuthEmbeddedUserService
import io.medatarun.auth.ports.needs.AuthClock
import io.medatarun.auth.ports.needs.UserStore
import java.nio.file.Path
import java.util.*

class AuthEmbeddedUserServiceImpl(
    private val bootstrapDirPath: Path,
    private val userStorage: UserStore,
    private val clock: AuthClock,
    private val passwordEncryptionIterations: Int
) : AuthEmbeddedUserService {

    private val bootstrapper = AuthEmbeddedBootstrapSecretImpl(bootstrapDirPath)
    private val authEmbeddedPwd = AuthEmbeddedPwd(passwordEncryptionIterations)

    override fun loadOrCreateBootstrapSecret(runOnce: (secret: String) -> Unit) {
        bootstrapper.loadOrCreateBootstrapSecret(runOnce)
    }

    override fun adminBootstrap(secret: String, login: String, fullname: String, password: String): User {
        val bootstrapState = bootstrapper.load() ?: throw AuthEmbeddedServiceBootstrapNotReadyException()
        if (bootstrapState.consumed) throw AuthEmbeddedBootstrapAlreadyConsumedException()
        if (bootstrapState.secret != secret) throw AuthEmbeddedBootstrapBadSecretException()

        val user = createEmbeddedUserInternal(
            id = UUID.randomUUID(),
            login = login,
            fullname = fullname,
            clearPassword = password,
            admin = true,
            bootstrap = true
        )

        bootstrapper.markBootstrapConsumed()
        return user
    }

    override fun createEmbeddedUser(login: String, fullname: String, clearPassword: String, admin: Boolean): User {
        return createEmbeddedUserInternal(UUID.randomUUID(), login, fullname, clearPassword, admin, false)
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

    override fun changeUserFullname(username: String, fullname: String) {
        userStorage.findByLogin(username) ?: throw AuthEmbeddedUserNotFoundException()
        userStorage.updateFullname(username, fullname)
    }

    override fun loginUser(username: String, password: String): User {
        val user = userStorage.findByLogin(username) ?: throw AuthEmbeddedBadCredentialsException()
        if (user.disabledDate != null) throw AuthEmbeddedBadCredentialsException()
        val valid = authEmbeddedPwd.verifyPassword(user.passwordHash, password)
        if (!valid) throw AuthEmbeddedBadCredentialsException()
        return user
    }
}