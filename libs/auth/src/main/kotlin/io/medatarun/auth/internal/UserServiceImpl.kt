package io.medatarun.auth.internal

import io.medatarun.auth.domain.*
import io.medatarun.auth.ports.exposed.UserService
import io.medatarun.auth.ports.needs.AuthClock
import io.medatarun.auth.ports.needs.UserStorage
import java.nio.file.Path
import java.util.*

class UserServiceImpl(
    private val bootstrapDirPath: Path,
    private val userStorage: UserStorage,
    private val clock: AuthClock,
    private val passwordEncryptionIterations: Int
) : UserService {

    private val bootstrapper = BootstrapSecretLifecycleImpl(bootstrapDirPath)
    private val userPasswordEncrypter = UserPasswordEncrypter(passwordEncryptionIterations)

    override fun loadOrCreateBootstrapSecret(runOnce: (secret: String) -> Unit) {
        bootstrapper.loadOrCreateBootstrapSecret(runOnce)
    }

    override fun adminBootstrap(secret: String, login: String, fullname: String, password: String): User {
        val bootstrapState = bootstrapper.load() ?: throw BootstrapSecretNotReadyException()
        if (bootstrapState.consumed) throw BootstrapSecretAlreadyConsumedException()
        if (bootstrapState.secret != secret) throw BootstrapSecretBadSecretException()

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
        val checkPasswordPolicy = userPasswordEncrypter.checkPasswordPolicy(clearPassword, login)
        if (checkPasswordPolicy is UserPasswordEncrypter.PasswordCheck.Fail)
            throw UserCreatePasswordFailException(checkPasswordPolicy.reason)
        val password = userPasswordEncrypter.hashPassword(clearPassword)

        val found = userStorage.findByLogin(login)
        if (found != null) throw UserAlreadyExistsException()

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
        val user = userStorage.findByLogin(username) ?: throw AuthUnauthorizedException()
        val valid = userPasswordEncrypter.verifyPassword(user.passwordHash, oldPassword)
        if (!valid) throw AuthUnauthorizedException()
        val policyCheck = userPasswordEncrypter.checkPasswordPolicy(newPassword, username)
        if (policyCheck is UserPasswordEncrypter.PasswordCheck.Fail)
            throw UserCreatePasswordFailException(policyCheck.reason)
        val newPasswordHash = userPasswordEncrypter.hashPassword(newPassword)
        userStorage.updatePassword(username, newPasswordHash)
    }

    override fun changeUserPassword(login: String, newPassword: String) {
        val user = userStorage.findByLogin(login) ?: throw UserNotFoundException()
        val policyCheck = userPasswordEncrypter.checkPasswordPolicy(newPassword, login)
        if (policyCheck is UserPasswordEncrypter.PasswordCheck.Fail)
            throw UserCreatePasswordFailException(policyCheck.reason)
        val newPasswordHash = userPasswordEncrypter.hashPassword(newPassword)
        userStorage.updatePassword(user.login, newPasswordHash)
    }

    override fun disableUser(username: String) {
        userStorage.disable(username, at = clock.now())
    }

    override fun changeUserFullname(username: String, fullname: String) {
        userStorage.findByLogin(username) ?: throw UserNotFoundException()
        userStorage.updateFullname(username, fullname)
    }

    override fun loginUser(username: String, password: String): User {
        val user = userStorage.findByLogin(username) ?: throw AuthUnauthorizedException()
        if (user.disabledDate != null) throw AuthUnauthorizedException()
        val valid = userPasswordEncrypter.verifyPassword(user.passwordHash, password)
        if (!valid) throw AuthUnauthorizedException()
        return user
    }
}