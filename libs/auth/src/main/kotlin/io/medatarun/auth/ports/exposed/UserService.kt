package io.medatarun.auth.ports.exposed

import io.medatarun.auth.domain.Fullname
import io.medatarun.auth.domain.User
import io.medatarun.auth.domain.Username

interface UserService {

    fun loadOrCreateBootstrapSecret(runOnce: (secret: String) -> Unit)
    fun adminBootstrap(secret: String, login: Username, fullname: Fullname, password: String): User
    fun createEmbeddedUser(login: Username, fullname: Fullname, clearPassword: String, admin: Boolean): User
    fun changeOwnPassword(username: Username, oldPassword: String, newPassword: String)
    fun changeUserPassword(login: Username, newPassword: String)
    fun disableUser(username: Username)
    fun changeUserFullname(username: Username, fullname: Fullname)
    fun loginUser(username: Username, password: String): User


}