package io.medatarun.auth.internal

import io.medatarun.auth.domain.*
import io.medatarun.auth.ports.exposed.BootstrapSecretLifecycle
import io.medatarun.auth.ports.exposed.UserService
import io.medatarun.auth.ports.needs.*
import java.util.*

class UserServiceImpl(
    private val userStorage: UserStorage,
    private val clock: AuthClock,
    private val passwordEncryptionIterations: Int,
    private val bootstrapper: BootstrapSecretLifecycle,
    private val userEvents: UserServiceEvents
) : UserService {


    private val userPasswordEncrypter = UserPasswordEncrypter(passwordEncryptionIterations)

    override fun loadOrCreateBootstrapSecret(runOnce: (secret: String) -> Unit) {
        bootstrapper.loadOrCreateBootstrapSecret(runOnce)
    }

    override fun adminBootstrap(secret: String, login: Username, fullname: Fullname, password: PasswordClear): User {
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
        userEvents.fire(UserEventCreated(user))
        return user
    }

    override fun createEmbeddedUser(login: Username, fullname: Fullname, clearPassword: PasswordClear, admin: Boolean): User {
        val user = createEmbeddedUserInternal(UUID.randomUUID(), login, fullname, clearPassword, admin, false)
        userEvents.fire(UserEventCreated(user))
        return user
    }

    fun createEmbeddedUserInternal(
        id: UUID,
        login: Username,
        fullname: Fullname,
        clearPassword: PasswordClear,
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

    override fun changeOwnPassword(username: Username, oldPassword: PasswordClear, newPassword: PasswordClear) {
        val user = userStorage.findByLogin(username) ?: throw AuthUnauthorizedException()
        val valid = userPasswordEncrypter.verifyPassword(user.passwordHash, oldPassword)
        if (!valid) throw AuthUnauthorizedException()
        val policyCheck = userPasswordEncrypter.checkPasswordPolicy(newPassword, username)
        if (policyCheck is UserPasswordEncrypter.PasswordCheck.Fail)
            throw UserCreatePasswordFailException(policyCheck.reason)
        val newPasswordHash = userPasswordEncrypter.hashPassword(newPassword)
        userStorage.updatePassword(username, newPasswordHash)
    }

    override fun changeUserPassword(login: Username, newPassword: PasswordClear) {
        val user = userStorage.findByLogin(login) ?: throw UserNotFoundException()
        val policyCheck = userPasswordEncrypter.checkPasswordPolicy(newPassword, login)
        if (policyCheck is UserPasswordEncrypter.PasswordCheck.Fail)
            throw UserCreatePasswordFailException(policyCheck.reason)
        val newPasswordHash = userPasswordEncrypter.hashPassword(newPassword)
        userStorage.updatePassword(user.login, newPasswordHash)
    }

    override fun disableUser(username: Username) {
        val now = clock.now()
        val user = userStorage.findByLogin(username)
        userStorage.disable(username, at = now)
        if (user != null) {
            userEvents.fire(UserEventDisabledChanged(user.login, now))
        }
    }

    override fun changeUserFullname(username: Username, fullname: Fullname) {
        val user = userStorage.findByLogin(username) ?: throw UserNotFoundException()
        userStorage.updateFullname(username, fullname)
        userEvents.fire(UserEventFullnameChanged(user.login, fullname))
    }

    override fun loginUser(username: Username, password: PasswordClear): User {
        val user = userStorage.findByLogin(username) ?: throw AuthUnauthorizedException()
        if (user.disabledDate != null) throw AuthUnauthorizedException()
        val valid = userPasswordEncrypter.verifyPassword(user.passwordHash, password)
        if (!valid) throw AuthUnauthorizedException()
        return user
    }
}
