package io.medatarun.auth.ports.needs

import io.medatarun.auth.domain.User
import io.medatarun.auth.domain.Username
import java.time.Instant

interface UserStorage {
    fun insert(
        id: String,
        login: Username,
        fullname: String,
        password: String,
        admin: Boolean,
        bootstrap: Boolean,
        disabledDate: Instant?
    )

    fun findByLogin(login: Username): User?

    fun updatePassword(login: Username, newPassword: String)

    fun disable(login: Username, at: Instant = Instant.now())
    fun updateFullname(username: Username, fullname: String)


}