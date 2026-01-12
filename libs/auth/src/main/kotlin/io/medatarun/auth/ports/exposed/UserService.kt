package io.medatarun.auth.ports.exposed

import io.medatarun.auth.domain.user.Fullname
import io.medatarun.auth.domain.user.PasswordClear
import io.medatarun.auth.domain.user.User
import io.medatarun.auth.domain.user.Username

interface UserService {

    fun loadOrCreateBootstrapSecret(runOnce: (secret: String) -> Unit)
    fun adminBootstrap(secret: String, login: Username, fullname: Fullname, password: PasswordClear): User
    fun createEmbeddedUser(login: Username, fullname: Fullname, clearPassword: PasswordClear, admin: Boolean): User
    fun changeOwnPassword(username: Username, oldPassword: PasswordClear, newPassword: PasswordClear)
    fun changeUserPassword(login: Username, newPassword: PasswordClear)
    fun disableUser(username: Username)
    fun changeUserFullname(username: Username, fullname: Fullname)
    fun loginUser(username: Username, password: PasswordClear): User


}