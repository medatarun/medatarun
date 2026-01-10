package io.medatarun.auth.ports.exposed

import io.medatarun.auth.domain.User

interface UserService {

    fun loadOrCreateBootstrapSecret(runOnce: (secret: String) -> Unit)
    fun adminBootstrap(secret: String, login: String, fullname: String, password: String): User
    fun createEmbeddedUser(login: String, fullname: String, clearPassword: String, admin: Boolean): User
    fun changeOwnPassword(username: String, oldPassword: String, newPassword: String)
    fun changeUserPassword(login: String, newPassword: String)
    fun disableUser(username: String)
    fun changeUserFullname(username: String, fullname: String)
    fun loginUser(username: String, password: String): User


}