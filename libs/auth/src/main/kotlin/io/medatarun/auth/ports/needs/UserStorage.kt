package io.medatarun.auth.ports.needs

import io.medatarun.auth.domain.user.Fullname
import io.medatarun.auth.domain.user.PasswordHash
import io.medatarun.auth.domain.user.User
import io.medatarun.auth.domain.user.Username
import java.time.Instant

interface UserStorage {
    fun insert(
        id: String,
        login: Username,
        fullname: Fullname,
        password: PasswordHash,
        admin: Boolean,
        bootstrap: Boolean,
        disabledDate: Instant?
    )

    fun findByLogin(login: Username): User?

    fun updatePassword(login: Username, newPassword: PasswordHash)

    fun disable(login: Username, at: Instant = Instant.now())
    fun updateFullname(username: Username, fullname: Fullname)


}